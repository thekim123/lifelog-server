package com.younghwan.lifelog.receipt;

import com.younghwan.lifelog.catalog.ItemNormalizationService;
import com.younghwan.lifelog.expense.ExpenseEntry;
import com.younghwan.lifelog.expense.ExpenseRepository;
import com.younghwan.lifelog.inventory.*;
import com.younghwan.lifelog.receipt.ReceiptDtos.*;
import com.younghwan.lifelog.receipt.ocr.OcrProvider;
import com.younghwan.lifelog.receipt.ocr.OcrResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final ExpenseRepository expenseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final OcrProvider ocrProvider;
    private final ItemNormalizationService itemNormalizationService;

    @Transactional
    public Map<String, Object> upload(MultipartFile file,
                                      BigDecimal totalAmount,
                                      String category,
                                      List<String> itemNames,
                                      List<BigDecimal> itemQuantities,
                                      List<String> itemUnits,
                                      List<BigDecimal> itemPrices) throws IOException {
        Path dir = Paths.get("uploads");
        Files.createDirectories(dir);
        Path target = dir.resolve(System.currentTimeMillis() + "_" + file.getOriginalFilename());
        file.transferTo(target);

        OcrResult ocr = ocrProvider.extract(file);
        List<ReceiptItemRequest> parsedItems = parseItems(itemNames, itemQuantities, itemUnits, itemPrices);

        Receipt receipt = Receipt.builder()
                .storeName(ocr.storeName() == null ? "UNKNOWN" : ocr.storeName())
                .totalAmount(zeroIfNull(totalAmount).compareTo(BigDecimal.ZERO) > 0 ? totalAmount : zeroIfNull(ocr.totalAmount()))
                .purchasedAt(ocr.purchasedAt() == null ? LocalDateTime.now() : ocr.purchasedAt())
                .rawOcrText(ocr.rawText())
                .imagePath(target.toString())
                .confirmed(false)
                .items(new ArrayList<>())
                .build();

        for (ReceiptItemRequest req : parsedItems) {
            String normalized = itemNormalizationService.normalize(req.itemName());
            ReceiptItem item = ReceiptItem.builder()
                    .receipt(receipt)
                    .itemName(normalized)
                    .quantity(req.quantity())
                    .unit(req.unit())
                    .unitPrice(req.unitPrice())
                    .totalPrice(req.totalPrice())
                    .build();
            receipt.getItems().add(item);
        }

        receipt = receiptRepository.save(receipt);

        expenseRepository.save(ExpenseEntry.builder()
                .category(category == null || category.isBlank() ? "미분류" : category)
                .amount(receipt.getTotalAmount())
                .memo("receiptId=" + receipt.getId())
                .spentAt(receipt.getPurchasedAt())
                .build());

        for (ReceiptItem item : receipt.getItems()) {
            applyInventory(item, "IN", "RECEIPT_UPLOAD");
        }

        return Map.of("receiptId", receipt.getId(), "message", "uploaded", "itemCount", receipt.getItems().size());
    }

    @Transactional(readOnly = true)
    public ReceiptDetailResponse detail(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new EntityNotFoundException("receipt not found: " + receiptId));

        List<ReceiptItemResponse> items = receipt.getItems().stream()
                .map(i -> new ReceiptItemResponse(i.getId(), i.getItemName(), i.getQuantity(), i.getUnit(), i.getUnitPrice(), i.getTotalPrice()))
                .toList();

        return new ReceiptDetailResponse(receipt.getId(), receipt.getStoreName(), receipt.getTotalAmount(),
                receipt.getPurchasedAt(), receipt.isConfirmed(), items, receipt.getRawOcrText());
    }

    @Transactional
    public ReceiptDetailResponse confirm(Long receiptId, ReceiptConfirmRequest req) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new EntityNotFoundException("receipt not found: " + receiptId));

        if (req.storeName() != null) receipt.setStoreName(req.storeName());
        if (req.totalAmount() != null) receipt.setTotalAmount(req.totalAmount());
        if (req.purchasedAt() != null) receipt.setPurchasedAt(req.purchasedAt());

        if (req.items() != null) {
            receipt.getItems().clear();
            for (ReceiptItemRequest itemReq : req.items()) {
                String normalized = itemNormalizationService.normalize(itemReq.itemName());
                ReceiptItem item = ReceiptItem.builder()
                        .receipt(receipt)
                        .itemName(normalized)
                        .quantity(itemReq.quantity())
                        .unit(itemReq.unit())
                        .unitPrice(itemReq.unitPrice())
                        .totalPrice(itemReq.totalPrice())
                        .build();
                receipt.getItems().add(item);
                applyInventory(item, "IN", "RECEIPT_CONFIRM");
            }
        }

        receipt.setConfirmed(true);
        receiptRepository.save(receipt);
        return detail(receiptId);
    }

    private void applyInventory(ReceiptItem item, String txType, String reason) {
        InventoryStock stock = inventoryRepository.findByItemNameIgnoreCase(item.getItemName())
                .orElse(InventoryStock.builder().itemName(item.getItemName()).quantity(BigDecimal.ZERO).unit(item.getUnit()).build());
        stock.setQuantity(stock.getQuantity().add(item.getQuantity()));
        stock.setUnit(item.getUnit());
        stock = inventoryRepository.save(stock);

        inventoryTransactionRepository.save(InventoryTransaction.builder()
                .inventoryStock(stock)
                .receiptItem(item)
                .quantityChange(item.getQuantity())
                .balanceAfter(stock.getQuantity())
                .txType(txType)
                .reason(reason)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    private List<ReceiptItemRequest> parseItems(List<String> names,
                                                List<BigDecimal> quantities,
                                                List<String> units,
                                                List<BigDecimal> prices) {
        List<ReceiptItemRequest> result = new ArrayList<>();
        if (names == null || names.isEmpty()) return result;
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name == null || name.isBlank()) continue;
            BigDecimal quantity = at(quantities, i, BigDecimal.ONE);
            String unit = at(units, i, "ea");
            BigDecimal totalPrice = at(prices, i, null);
            result.add(new ReceiptItemRequest(name, quantity, unit, null, totalPrice));
        }
        return result;
    }

    private <T> T at(List<T> list, int idx, T defaultVal) {
        return (list != null && idx < list.size() && list.get(idx) != null) ? list.get(idx) : defaultVal;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

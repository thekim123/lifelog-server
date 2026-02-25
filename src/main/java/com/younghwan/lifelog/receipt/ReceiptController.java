package com.younghwan.lifelog.receipt;

import com.younghwan.lifelog.receipt.ReceiptDtos.ReceiptConfirmRequest;
import com.younghwan.lifelog.receipt.ReceiptDtos.ReceiptDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {
    private final ReceiptService receiptService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(defaultValue = "0") BigDecimal totalAmount,
                                                      @RequestParam(defaultValue = "미분류") String category,
                                                      @RequestParam(required = false) List<String> itemName,
                                                      @RequestParam(required = false) List<BigDecimal> itemQuantity,
                                                      @RequestParam(required = false) List<String> itemUnit,
                                                      @RequestParam(required = false) List<BigDecimal> itemPrice) throws IOException {
        return ResponseEntity.ok(receiptService.upload(file, totalAmount, category, itemName, itemQuantity, itemUnit, itemPrice));
    }

    @GetMapping("/{receiptId}")
    public ResponseEntity<ReceiptDetailResponse> detail(@PathVariable Long receiptId) {
        return ResponseEntity.ok(receiptService.detail(receiptId));
    }

    @PutMapping("/{receiptId}/confirm")
    public ResponseEntity<ReceiptDetailResponse> confirm(@PathVariable Long receiptId,
                                                         @RequestBody ReceiptConfirmRequest request) {
        return ResponseEntity.ok(receiptService.confirm(receiptId, request));
    }
}

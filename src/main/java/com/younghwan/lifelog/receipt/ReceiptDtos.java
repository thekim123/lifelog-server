package com.younghwan.lifelog.receipt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReceiptDtos {
    public record ReceiptItemRequest(
            @NotBlank String itemName,
            @NotNull BigDecimal quantity,
            @NotBlank String unit,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}

    public record ReceiptItemResponse(
            Long id,
            String itemName,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}

    public record ReceiptDetailResponse(
            Long receiptId,
            String storeName,
            BigDecimal totalAmount,
            LocalDateTime purchasedAt,
            boolean confirmed,
            List<ReceiptItemResponse> items,
            String rawOcrText
    ) {}

    public record ReceiptConfirmRequest(
            String storeName,
            BigDecimal totalAmount,
            LocalDateTime purchasedAt,
            List<ReceiptItemRequest> items
    ) {}
}

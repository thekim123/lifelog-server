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

    public record ReceiptSummaryResponse(
            Long receiptId,
            Long householdId,
            String householdName,
            String uploadedBy,
            String storeName,
            BigDecimal totalAmount,
            LocalDateTime purchasedAt,
            boolean confirmed,
            int itemCount,
            OcrStatus ocrStatus,
            String ocrRequestId,
            String ocrError,
            LocalDateTime lastOcrAt
    ) {}

    public record ReceiptDetailResponse(
            Long receiptId,
            Long householdId,
            String householdName,
            String uploadedBy,
            String storeName,
            BigDecimal totalAmount,
            LocalDateTime purchasedAt,
            boolean confirmed,
            List<ReceiptItemResponse> items,
            String rawOcrText,
            OcrStatus ocrStatus,
            String ocrRequestId,
            String ocrError,
            LocalDateTime lastOcrAt
    ) {}

    public record ReceiptUploadResponse(
            Long receiptId,
            Long householdId,
            String householdName,
            String message,
            int itemCount,
            OcrStatus ocrStatus,
            String ocrRequestId
    ) {}

    public record ReceiptConfirmRequest(
            String storeName,
            BigDecimal totalAmount,
            LocalDateTime purchasedAt,
            List<ReceiptItemRequest> items
    ) {}
}

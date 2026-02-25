package com.younghwan.lifelog.receipt.ocr;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OcrResult(
        String rawText,
        String storeName,
        LocalDateTime purchasedAt,
        BigDecimal totalAmount,
        List<OcrItem> items
) {
    @Builder
    public record OcrItem(String itemName, BigDecimal quantity, String unit, BigDecimal unitPrice, BigDecimal totalPrice) {}
}

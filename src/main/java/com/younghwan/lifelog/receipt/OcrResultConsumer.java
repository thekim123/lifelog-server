package com.younghwan.lifelog.receipt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.younghwan.lifelog.catalog.ItemNormalizationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OcrResultConsumer {
    private final ObjectMapper objectMapper;
    private final ReceiptRepository receiptRepository;
    private final ProcessedOcrEventRepository processedOcrEventRepository;
    private final ReceiptEventPublisher receiptEventPublisher;
    private final ItemNormalizationService itemNormalizationService;

    @Value("${app.events.ocr-completed-topic:ocr.completed}")
    private String ocrCompletedTopic;

    @Value("${app.events.ocr-failed-topic:ocr.failed}")
    private String ocrFailedTopic;

    @KafkaListener(topics = "${app.events.ocr-completed-topic:ocr.completed}", groupId = "${app.events.ocr-consumer-group:lifelog-ocr-consumer}")
    public void consumeCompleted(String payload) {
        consume(payload, true, ocrCompletedTopic);
    }

    @KafkaListener(topics = "${app.events.ocr-failed-topic:ocr.failed}", groupId = "${app.events.ocr-consumer-group:lifelog-ocr-consumer}")
    public void consumeFailed(String payload) {
        consume(payload, false, ocrFailedTopic);
    }

    private void consume(String payload, boolean completed, String sourceTopic) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            handle(node, completed);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("Malformed OCR event on {}: {}", sourceTopic, e.getMessage());
            receiptEventPublisher.publishDlq(sourceTopic, "MALFORMED_EVENT", payload, e.getMessage());
        } catch (Exception e) {
            log.warn("Unrecoverable OCR event on {}: {}", sourceTopic, e.getMessage());
            receiptEventPublisher.publishDlq(sourceTopic, "UNRECOVERABLE_EVENT", payload, e.getMessage());
        }
    }

    @Transactional
    protected void handle(JsonNode node, boolean completed) {
        String eventId = text(node, "eventId");
        String requestId = text(node, "requestId");
        Long receiptId = longValue(node, "receiptId");
        if ((eventId == null || eventId.isBlank()) && (requestId == null || requestId.isBlank())) {
            throw new IllegalArgumentException("eventId/requestId missing");
        }

        String eventKey = (eventId != null && !eventId.isBlank()) ? eventId : requestId;
        if (processedOcrEventRepository.existsByEventKey(eventKey)) {
            return;
        }

        Receipt receipt = resolveReceipt(receiptId, requestId);
        if (completed) {
            receipt.setOcrStatus(OcrStatus.DONE);
            receipt.setOcrError(null);
            receipt.setRawOcrText(text(node, "rawText"));

            String storeName = text(node, "storeName");
            if (storeName != null && !storeName.isBlank()) receipt.setStoreName(storeName);

            BigDecimal totalAmount = decimalValue(node, "totalAmount");
            if (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) receipt.setTotalAmount(totalAmount);

            LocalDateTime purchasedAt = dateTime(node, "purchasedAt");
            if (purchasedAt != null) receipt.setPurchasedAt(purchasedAt);

            JsonNode itemsNode = node.get("items");
            if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                receipt.getItems().clear();
                BigDecimal sum = BigDecimal.ZERO;
                for (JsonNode itemNode : itemsNode) {
                    String itemName = text(itemNode, "itemName");
                    if (itemName == null || itemName.isBlank()) continue;
                    String normalized = itemNormalizationService.normalize(itemName);
                    BigDecimal qty = decimalValue(itemNode, "quantity");
                    if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) qty = BigDecimal.ONE;
                    String unit = text(itemNode, "unit");
                    if (unit == null || unit.isBlank()) unit = "ea";
                    BigDecimal unitPrice = decimalValue(itemNode, "unitPrice");
                    BigDecimal totalPrice = decimalValue(itemNode, "totalPrice");
                    if (totalPrice == null && unitPrice != null) {
                        totalPrice = unitPrice.multiply(qty);
                    }
                    if (totalPrice != null) sum = sum.add(totalPrice);

                    ReceiptItem item = ReceiptItem.builder()
                            .receipt(receipt)
                            .itemName(normalized)
                            .quantity(qty)
                            .unit(unit)
                            .unitPrice(unitPrice)
                            .totalPrice(totalPrice)
                            .build();
                    receipt.getItems().add(item);
                }
                if ((receipt.getTotalAmount() == null || receipt.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
                        && sum.compareTo(BigDecimal.ZERO) > 0) {
                    receipt.setTotalAmount(sum);
                }
            }
        } else {
            receipt.setOcrStatus(OcrStatus.FAILED);
            receipt.setOcrError(text(node, "error"));
        }
        receipt.setLastOcrAt(LocalDateTime.now());
        receiptRepository.save(receipt);

        try {
            processedOcrEventRepository.save(ProcessedOcrEvent.builder()
                    .eventKey(eventKey)
                    .eventType(completed ? "OCR_COMPLETED" : "OCR_FAILED")
                    .processedAt(LocalDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException ignored) {
            // duplicate delivery raced; idempotent by unique key
        }
    }

    private Receipt resolveReceipt(Long receiptId, String requestId) {
        if (receiptId != null) {
            return receiptRepository.findById(receiptId)
                    .orElseThrow(() -> new EntityNotFoundException("receipt not found: " + receiptId));
        }
        if (requestId != null && !requestId.isBlank()) {
            return receiptRepository.findByOcrRequestId(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("receipt not found by requestId: " + requestId));
        }
        throw new EntityNotFoundException("Unable to resolve receipt");
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asLong();
    }

    private BigDecimal decimalValue(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        try {
            return new BigDecimal(v.asText());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime dateTime(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

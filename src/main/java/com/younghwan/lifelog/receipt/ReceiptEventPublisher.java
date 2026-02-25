package com.younghwan.lifelog.receipt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceiptEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.events.enabled:true}")
    private boolean enabled;

    @Value("${app.events.ocr-requested-topic:ocr.requested}")
    private String ocrRequestedTopic;

    @Value("${app.events.ocr-dlq-topic:ocr.dlq}")
    private String ocrDlqTopic;

    public boolean publishOcrRequested(Receipt receipt) {
        Map<String, Object> event = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "OCR_REQUESTED",
                "requestId", receipt.getOcrRequestId(),
                "receiptId", receipt.getId(),
                "householdId", receipt.getHousehold() == null ? null : receipt.getHousehold().getId(),
                "uploadedByUserId", receipt.getUploadedBy() == null ? null : receipt.getUploadedBy().getId(),
                "imagePath", receipt.getImagePath(),
                "occurredAt", LocalDateTime.now().toString()
        );
        return publish(ocrRequestedTopic, String.valueOf(receipt.getId()), event);
    }

    public void publishDlq(String sourceTopic, String reason, String payload, String errorMessage) {
        Map<String, Object> event = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "sourceTopic", sourceTopic,
                "reason", reason,
                "payload", payload == null ? "" : payload,
                "errorMessage", errorMessage == null ? "" : errorMessage,
                "occurredAt", LocalDateTime.now().toString()
        );
        publish(ocrDlqTopic, sourceTopic, event);
    }

    private boolean publish(String topic, String key, Object event) {
        if (!enabled) return false;
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, payload);
            return true;
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event for topic {}: {}", topic, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Failed to publish event to topic {}: {}", topic, e.getMessage());
            return false;
        }
    }
}

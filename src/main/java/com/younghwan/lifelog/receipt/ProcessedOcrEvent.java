package com.younghwan.lifelog.receipt;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "processed_ocr_event", uniqueConstraints = {
        @UniqueConstraint(name = "uk_processed_ocr_event_key", columnNames = "eventKey")
})
public class ProcessedOcrEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String eventKey;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}

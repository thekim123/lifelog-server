package com.younghwan.lifelog.receipt;

import com.younghwan.lifelog.auth.UserAccount;
import com.younghwan.lifelog.common.BaseEntity;
import com.younghwan.lifelog.household.Household;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    @Lob
    private String rawOcrText;

    private String imagePath;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OcrStatus ocrStatus = OcrStatus.PENDING;

    private String ocrRequestId;

    @Lob
    private String ocrError;

    private LocalDateTime lastOcrAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean confirmed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private UserAccount uploadedBy;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceiptItem> items = new ArrayList<>();
}

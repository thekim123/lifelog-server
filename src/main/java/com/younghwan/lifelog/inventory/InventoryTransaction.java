package com.younghwan.lifelog.inventory;

import com.younghwan.lifelog.common.BaseEntity;
import com.younghwan.lifelog.receipt.ReceiptItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_stock_id")
    private InventoryStock inventoryStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_item_id")
    private ReceiptItem receiptItem;

    @Column(nullable = false)
    private BigDecimal quantityChange;

    private BigDecimal balanceAfter;

    @Column(nullable = false)
    private String txType;

    private String reason;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}

package com.younghwan.lifelog.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryDtos {
    public record InventoryStockResponse(
            Long id,
            String itemName,
            BigDecimal quantity,
            String unit,
            LocalDate expiresAt
    ) {
        public static InventoryStockResponse from(InventoryStock stock) {
            return new InventoryStockResponse(
                    stock.getId(),
                    stock.getItemName(),
                    stock.getQuantity(),
                    stock.getUnit(),
                    stock.getExpiresAt()
            );
        }
    }
}

package com.younghwan.lifelog.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class ExpenseDtos {
    public record ExpenseResponse(
            Long id,
            String category,
            BigDecimal amount,
            String memo,
            LocalDateTime spentAt
    ) {
        public static ExpenseResponse from(ExpenseEntry entry) {
            return new ExpenseResponse(
                    entry.getId(),
                    entry.getCategory(),
                    entry.getAmount(),
                    entry.getMemo(),
                    entry.getSpentAt()
            );
        }
    }

    public record MonthlySummaryResponse(
            int year,
            int month,
            BigDecimal total,
            int count,
            Map<String, BigDecimal> byCategory
    ) {}
}

package com.younghwan.lifelog.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<ExpenseDtos.ExpenseResponse> list() {
        return expenseRepository.findAll().stream()
                .map(ExpenseDtos.ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseDtos.MonthlySummaryResponse monthlySummary(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDateTime from = firstDay.atStartOfDay();
        LocalDateTime to = firstDay.plusMonths(1).atStartOfDay();

        List<ExpenseEntry> rows = expenseRepository.findBySpentAtBetween(from, to);
        BigDecimal total = rows.stream().map(ExpenseEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        var byCategory = rows.stream().collect(Collectors.groupingBy(
                ExpenseEntry::getCategory,
                Collectors.reducing(BigDecimal.ZERO, ExpenseEntry::getAmount, BigDecimal::add)
        ));

        return new ExpenseDtos.MonthlySummaryResponse(year, month, total, rows.size(), byCategory);
    }
}

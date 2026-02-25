package com.younghwan.lifelog.expense;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void monthlySummary_aggregatesTotalCountAndCategoryBreakdown() {
        ExpenseEntry food = ExpenseEntry.builder()
                .category("FOOD")
                .amount(new BigDecimal("10000"))
                .spentAt(LocalDateTime.of(2026, 2, 1, 10, 0))
                .build();
        ExpenseEntry transport = ExpenseEntry.builder()
                .category("TRANSPORT")
                .amount(new BigDecimal("3000"))
                .spentAt(LocalDateTime.of(2026, 2, 2, 8, 0))
                .build();
        ExpenseEntry food2 = ExpenseEntry.builder()
                .category("FOOD")
                .amount(new BigDecimal("2500"))
                .spentAt(LocalDateTime.of(2026, 2, 3, 12, 0))
                .build();

        given(expenseRepository.findBySpentAtBetween(
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 3, 1, 0, 0)
        )).willReturn(List.of(food, transport, food2));

        ExpenseDtos.MonthlySummaryResponse summary = expenseService.monthlySummary(2026, 2);

        assertThat(summary.year()).isEqualTo(2026);
        assertThat(summary.month()).isEqualTo(2);
        assertThat(summary.count()).isEqualTo(3);
        assertThat(summary.total()).isEqualTo(new BigDecimal("15500"));

        Map<String, BigDecimal> byCategory = summary.byCategory();
        assertThat(byCategory.get("FOOD")).isEqualTo(new BigDecimal("12500"));
        assertThat(byCategory.get("TRANSPORT")).isEqualTo(new BigDecimal("3000"));

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(expenseRepository).findBySpentAtBetween(fromCaptor.capture(), toCaptor.capture());
        assertThat(fromCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));
        assertThat(toCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
    }
}

package com.younghwan.lifelog.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @GetMapping
    public List<ExpenseDtos.ExpenseResponse> list() {
        return expenseService.list();
    }

    @GetMapping("/summary/monthly")
    public ExpenseDtos.MonthlySummaryResponse monthlySummary(@RequestParam int year, @RequestParam int month) {
        return expenseService.monthlySummary(year, month);
    }
}

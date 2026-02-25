package com.younghwan.lifelog.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseEntry, Long> {
    List<ExpenseEntry> findBySpentAtBetween(LocalDateTime from, LocalDateTime to);
}

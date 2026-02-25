package com.younghwan.lifelog.receipt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByHouseholdIdOrderByPurchasedAtDesc(Long householdId);
    Optional<Receipt> findByOcrRequestId(String ocrRequestId);
}

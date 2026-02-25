package com.younghwan.lifelog.receipt;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOcrEventRepository extends JpaRepository<ProcessedOcrEvent, Long> {
    boolean existsByEventKey(String eventKey);
}

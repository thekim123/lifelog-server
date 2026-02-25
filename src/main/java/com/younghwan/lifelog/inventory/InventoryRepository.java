package com.younghwan.lifelog.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryStock, Long> {
    Optional<InventoryStock> findByItemNameIgnoreCase(String itemName);
}

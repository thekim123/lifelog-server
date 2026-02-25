package com.younghwan.lifelog.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryDtos.InventoryStockResponse> stocks() {
        return inventoryRepository.findAll().stream()
                .map(InventoryDtos.InventoryStockResponse::from)
                .toList();
    }
}

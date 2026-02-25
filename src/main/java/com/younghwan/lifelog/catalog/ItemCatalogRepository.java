package com.younghwan.lifelog.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCatalogRepository extends JpaRepository<ItemCatalog, Long> {
    Optional<ItemCatalog> findByCanonicalNameIgnoreCase(String canonicalName);
}

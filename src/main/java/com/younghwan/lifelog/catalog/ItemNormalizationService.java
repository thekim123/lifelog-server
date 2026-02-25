package com.younghwan.lifelog.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ItemNormalizationService {
    private final ItemCatalogRepository itemCatalogRepository;

    public String normalize(String rawName) {
        if (rawName == null || rawName.isBlank()) return rawName;
        String candidate = rawName.trim();
        String lowered = candidate.toLowerCase(Locale.ROOT);

        return itemCatalogRepository.findAll().stream()
                .filter(c -> matches(c, lowered))
                .map(ItemCatalog::getCanonicalName)
                .findFirst()
                .orElse(candidate);
    }

    private boolean matches(ItemCatalog catalog, String lowered) {
        if (catalog.getCanonicalName() != null && catalog.getCanonicalName().equalsIgnoreCase(lowered)) {
            return true;
        }
        return Arrays.stream((catalog.getAliases() == null ? "" : catalog.getAliases()).split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .anyMatch(alias -> alias.equalsIgnoreCase(lowered));
    }
}

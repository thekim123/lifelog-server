package com.younghwan.lifelog.recommendation;

import com.younghwan.lifelog.inventory.InventoryRepository;
import com.younghwan.lifelog.inventory.InventoryStock;
import com.younghwan.lifelog.recipe.Recipe;
import com.younghwan.lifelog.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecipeRepository recipeRepository;
    private final InventoryRepository inventoryRepository;

    public List<Map<String, Object>> today() {
        List<InventoryStock> stocks = inventoryRepository.findAll();
        Set<String> have = stocks.stream()
                .map(s -> s.getItemName().toLowerCase())
                .collect(Collectors.toSet());

        return recipeRepository.findAll().stream()
                .map(recipe -> scoreRecipe(recipe, stocks, have))
                .sorted((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")))
                .toList();
    }

    private Map<String, Object> scoreRecipe(Recipe recipe, List<InventoryStock> stocks, Set<String> have) {
        List<String> req = Arrays.stream(Optional.ofNullable(recipe.getRequiredItems()).orElse("").split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .toList();

        long matched = req.stream().filter(have::contains).count();
        List<String> missing = req.stream().filter(x -> !have.contains(x)).toList();

        double matchRatio = req.isEmpty() ? 0.0 : (matched * 1.0 / req.size());
        int missingCount = missing.size();
        double expiringSoonBonus = computeExpiringSoonBonus(stocks, req);

        double score = (matchRatio * 100.0) - (missingCount * 8.0) + expiringSoonBonus;

        return Map.of(
                "recipeId", recipe.getId(),
                "title", recipe.getTitle(),
                "score", Math.max(0.0, score),
                "matchRatio", matchRatio,
                "missingCount", missingCount,
                "expiringSoonBonus", expiringSoonBonus,
                "missing", missing
        );
    }

    private double computeExpiringSoonBonus(List<InventoryStock> stocks, List<String> requiredItems) {
        LocalDate threshold = LocalDate.now().plusDays(3);
        long expiringAndRelevant = stocks.stream()
                .filter(s -> s.getExpiresAt() != null && !s.getExpiresAt().isAfter(threshold))
                .map(s -> s.getItemName().toLowerCase())
                .filter(requiredItems::contains)
                .count();

        return Math.min(20.0, expiringAndRelevant * 5.0);
    }
}

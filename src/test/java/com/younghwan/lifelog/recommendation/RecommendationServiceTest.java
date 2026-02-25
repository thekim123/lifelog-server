package com.younghwan.lifelog.recommendation;

import com.younghwan.lifelog.inventory.InventoryRepository;
import com.younghwan.lifelog.inventory.InventoryStock;
import com.younghwan.lifelog.recipe.Recipe;
import com.younghwan.lifelog.recipe.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void today_appliesMatchRatioMissingPenaltyAndExpiringBonus() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Omelette")
                .requiredItems("milk,egg,onion")
                .build();

        InventoryStock milk = InventoryStock.builder()
                .id(1L)
                .itemName("milk")
                .quantity(BigDecimal.ONE)
                .unit("EA")
                .expiresAt(LocalDate.now().plusDays(1))
                .build();
        InventoryStock rice = InventoryStock.builder()
                .id(2L)
                .itemName("rice")
                .quantity(BigDecimal.ONE)
                .unit("EA")
                .expiresAt(LocalDate.now().plusDays(1))
                .build();

        given(recipeRepository.findAll()).willReturn(List.of(recipe));
        given(inventoryRepository.findAll()).willReturn(List.of(milk, rice));

        List<Map<String, Object>> result = recommendationService.today();
        Map<String, Object> scored = result.getFirst();

        assertThat((double) scored.get("matchRatio")).isCloseTo(1.0 / 3.0, offset(0.0001));
        assertThat(scored.get("missingCount")).isEqualTo(2);
        assertThat((double) scored.get("expiringSoonBonus")).isEqualTo(5.0);
        assertThat((double) scored.get("score")).isCloseTo(22.3333, offset(0.001));
    }

    @Test
    void today_capsExpiringBonusAt20() {
        Recipe recipe = Recipe.builder()
                .id(2L)
                .title("Big Salad")
                .requiredItems("a,b,c,d,e")
                .build();

        List<InventoryStock> stocks = List.of("a", "b", "c", "d", "e").stream()
                .map(item -> InventoryStock.builder()
                        .itemName(item)
                        .quantity(BigDecimal.ONE)
                        .unit("EA")
                        .expiresAt(LocalDate.now())
                        .build())
                .toList();

        given(recipeRepository.findAll()).willReturn(List.of(recipe));
        given(inventoryRepository.findAll()).willReturn(stocks);

        Map<String, Object> scored = recommendationService.today().getFirst();

        assertThat((double) scored.get("expiringSoonBonus")).isEqualTo(20.0);
        assertThat((double) scored.get("score")).isEqualTo(120.0);
    }
}

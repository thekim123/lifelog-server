package com.younghwan.lifelog.recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public List<RecipeDtos.RecipeResponse> list() {
        return recipeRepository.findAll().stream()
                .map(RecipeDtos.RecipeResponse::from)
                .toList();
    }

    @Transactional
    public RecipeDtos.RecipeResponse create(RecipeDtos.CreateRecipeRequest request) {
        Recipe recipe = Recipe.builder()
                .title(request.title())
                .instructions(request.instructions())
                .requiredItems(request.requiredItems())
                .build();

        return RecipeDtos.RecipeResponse.from(recipeRepository.save(recipe));
    }
}

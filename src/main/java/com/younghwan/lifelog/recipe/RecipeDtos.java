package com.younghwan.lifelog.recipe;

import jakarta.validation.constraints.NotBlank;

public class RecipeDtos {
    public record RecipeResponse(
            Long id,
            String title,
            String instructions,
            String requiredItems
    ) {
        public static RecipeResponse from(Recipe recipe) {
            return new RecipeResponse(
                    recipe.getId(),
                    recipe.getTitle(),
                    recipe.getInstructions(),
                    recipe.getRequiredItems()
            );
        }
    }

    public record CreateRecipeRequest(
            @NotBlank(message = "title is required") String title,
            String instructions,
            @NotBlank(message = "requiredItems is required") String requiredItems
    ) {}
}

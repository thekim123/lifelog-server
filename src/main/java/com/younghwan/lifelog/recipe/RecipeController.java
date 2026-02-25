package com.younghwan.lifelog.recipe;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping
    public List<RecipeDtos.RecipeResponse> list() {
        return recipeService.list();
    }

    @PostMapping
    public RecipeDtos.RecipeResponse create(@Valid @RequestBody RecipeDtos.CreateRecipeRequest request) {
        return recipeService.create(request);
    }
}

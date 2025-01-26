package net.jacobwasbeast.supernatural.api;

import java.util.Optional;


public class Recipe<T> {
    private RecipeType type;
    private T recipeMatrix;

    public Recipe(RecipeType type, T recipeMatrix) {
        this.type = type;
        this.recipeMatrix = recipeMatrix;
    }

    public static Recipe<int[][]> createChalkRecipe(int[][] recipe) {
        return new Recipe<>(RecipeType.CHALK, recipe);
    }

    public RecipeType getType() {
        return type;
    }

    public T getRecipeMatrix() {
        return recipeMatrix;
    }
}

package net.jacobwasbeast.supernatural.api;

import net.minecraft.util.Identifier;

public class Ritual {
    public String name;
    public String description;
    public Identifier reference;
    public Recipe recipe;
    public int radius;
    public Ritual(String name, String description, Identifier reference, Recipe recipe, int radius) {
        this.name = name;
        this.description = description;
        this.reference = reference;
        this.recipe = recipe;
        this.radius = radius;
    }

    public Recipe getRecipe() {
        return recipe;
    }
}

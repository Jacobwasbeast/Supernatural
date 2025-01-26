package net.jacobwasbeast.supernatural.api;

import net.minecraft.util.Identifier;

public class Ritual {
    public String name;
    public String description;
    public Identifier reference;
    public Ritual(String name, String description, Identifier reference) {
        this.name = name;
        this.description = description;
        this.reference = reference;
    }
}

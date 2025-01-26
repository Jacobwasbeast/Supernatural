package net.jacobwasbeast.supernatural.api;

public class RitualMatch {
    private final Ritual ritual;
    private final int rotation; // 0: 0°, 1: 90°, 2: 180°, 3: 270°

    public RitualMatch(Ritual ritual, int rotation) {
        this.ritual = ritual;
        this.rotation = rotation;
    }

    public Ritual getRitual() {
        return ritual;
    }

    public int getRotation() {
        return rotation;
    }
}
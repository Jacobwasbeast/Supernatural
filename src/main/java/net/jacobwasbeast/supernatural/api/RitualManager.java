package net.jacobwasbeast.supernatural.api;

import net.jacobwasbeast.supernatural.SupernaturalMain;

import java.util.HashMap;

public class RitualManager {
    public HashMap<String, Ritual> rituals = new HashMap<String, Ritual>();
    public static RitualManager instance;
    public static RitualManager getInstance() {
        if (instance == null) {
            instance = new RitualManager();
        }
        return instance;
    }
    public RitualManager() {
        rituals.put("DEVILS_TRAP", new Ritual("Devil's Trap", "A trap used to capture demons", SupernaturalMain.id("devilstrap")));
        rituals.put("PROTECTION_CIRCLE", new Ritual("Protection's Circle", "A circle used to protect against demons", SupernaturalMain.id("protectioncircle")));
    }
}

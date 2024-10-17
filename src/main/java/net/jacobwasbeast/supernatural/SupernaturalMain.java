package net.jacobwasbeast.supernatural;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class SupernaturalMain implements ModInitializer {

    @Override
    public void onInitialize() {
        ModEntities.registerEntities();
        ModItems.registerItems();
    }

    public static Identifier id(String path) {
        return new Identifier("supernatural", path);
    }
}

package net.jacobwasbeast.supernatural;

import jdk.jshell.Snippet;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.jacobwasbeast.supernatural.items.ColtItem;
import net.jacobwasbeast.supernatural.items.Psalm;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static final Item COLT = Registry.register(
            Registries.ITEM,
            SupernaturalMain.id("colt"),
            new ColtItem(new Item.Settings().maxCount(1))
    );
    public static final Item COLTBULLET = Registry.register(
            Registries.ITEM,
            SupernaturalMain.id("colt_bullet"),
            new Item(new Item.Settings().maxCount(16))
    );
    public static final Item PSALM = Registry.register(
            Registries.ITEM,
            SupernaturalMain.id("psalm"),
            new Psalm(new Item.Settings().maxCount(1))
    );
    public static final Item DEMON_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            SupernaturalMain.id("demon_spawn_egg"),
            new SpawnEggItem(ModEntities.DEMON, 0x5d1009, 0x746261, new FabricItemSettings().maxCount(64))
    );
    public static void registerItems() {
        // Register other items here.
    }
}
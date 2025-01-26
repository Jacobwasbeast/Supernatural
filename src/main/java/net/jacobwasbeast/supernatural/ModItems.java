package net.jacobwasbeast.supernatural;

import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.jacobwasbeast.supernatural.items.ColtItem;
import net.jacobwasbeast.supernatural.items.Psalm;
import net.jacobwasbeast.supernatural.items.RitualBook;
import net.jacobwasbeast.supernatural.items.RitualCreator;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;

public class ModItems implements ItemRegistryContainer {
    public static final Item COLT = new ColtItem(new Item.Settings().maxCount(1));
    public static final Item COLTBULLET = new Item(new Item.Settings().maxCount(16));
    public static final Item PSALM = new Psalm(new Item.Settings().maxCount(1));
    public static final Item DEMON_SPAWN_EGG = new SpawnEggItem(ModEntities.DEMON, 0x5d1009, 0x746261, new FabricItemSettings().maxCount(64));
    public static final Item RITUAL_BOOK = new RitualBook(new Item.Settings().maxCount(1));
    public static final Item RITUAL_CREATOR = new RitualCreator(new Item.Settings().maxCount(1).maxDamage(100));
}
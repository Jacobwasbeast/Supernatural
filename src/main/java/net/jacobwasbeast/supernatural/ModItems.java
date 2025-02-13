package net.jacobwasbeast.supernatural;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.jacobwasbeast.supernatural.items.*;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.DyeColor;

public class ModItems implements ItemRegistryContainer {
    public static final Item COLT = new ColtItem(new Item.Settings().maxCount(1));
    public static final Item COLT_BULLET = new Item(new Item.Settings().maxCount(16));
    public static final Item RUBY_KNIFE = new RubyKnife(ToolMaterials.NETHERITE,1,0f,new Item.Settings().maxCount(1));
    public static final Item SALT_SHAKER = new SaltItem(new Item.Settings().maxCount(1).maxDamage(10), DyeColor.WHITE);
    public static final Item SALT_BAG = new SaltItem(new Item.Settings().maxCount(1).maxDamage(100), DyeColor.WHITE);
    public static final Item RITUAL_CREATOR = new RitualCreator(new Item.Settings().maxCount(1).maxDamage(100));
    public static final Item RITUAL_BOOK = new RitualBook(new Item.Settings().maxCount(1));
    public static final Item PSALM = new Psalm(new Item.Settings().maxCount(1));
    public static final Item DEMON_SPAWN_EGG = new SpawnEggItem(ModEntities.DEMON, 0x5d1009, 0x746261, new FabricItemSettings().maxCount(64));
    public static final Item DEMON_VILLAGER_SPAWN_EGG = new SpawnEggItem(ModEntities.DEMON_VILLAGER, 0x3d1009, 0x526261, new FabricItemSettings().maxCount(64));
}
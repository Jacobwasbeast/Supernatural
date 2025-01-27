package net.jacobwasbeast.supernatural;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.item.Item;

public class ModMisc {
    public static final OwoItemGroup GROUP = OwoItemGroup
            .builder(SupernaturalMain.id("group"), () -> Icon.of(ModItems.COLT))
            .build();
    public static void registerMisc() {
        GROUP.addCustomTab(Icon.of(ModItems.RUBY_KNIFE),"Supernatural",(displayContext, entries) -> {
            for (var field : ModItems.class.getDeclaredFields()) {
                if (field.getType() == Item.class) {
                    try {
                        Item item = (Item) field.get(null);
                        entries.add(item);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        },true);
        GROUP.initialize();
    }
}

package net.jacobwasbeast.supernatural;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.jacobwasbeast.supernatural.blocks.ChalkSymbolBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static final Block CHALK_SYMBOL = new ChalkSymbolBlock(FabricBlockSettings.of().strength(1.0f).noCollision());

    public static void registerBlocks() {
        // Register the chalk symbol block
        Registry.register(Registries.BLOCK, SupernaturalMain.id("chalk_symbol"), CHALK_SYMBOL);
    }

}

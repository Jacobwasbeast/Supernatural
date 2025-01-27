package net.jacobwasbeast.supernatural;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.jacobwasbeast.supernatural.blocks.RitualChalk;
import net.jacobwasbeast.supernatural.blocks.Salt;
import net.jacobwasbeast.supernatural.blocks.entities.RitualChalkEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block CHALK_SYMBOL = new RitualChalk(FabricBlockSettings.of().noCollision().nonOpaque().sounds(BlockSoundGroup.GRAVEL).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().strength(1.0f).noCollision(), DyeColor.BLACK);
    public static final Block SALT = new Salt(FabricBlockSettings.of().noCollision().nonOpaque().sounds(BlockSoundGroup.GRAVEL).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().strength(1.0f).noCollision());
    public static final BlockEntityType<RitualChalkEntity> CHALK_ENTITY = register(
            "chalk_entity",
            BlockEntityType.Builder.create(RitualChalkEntity::new, CHALK_SYMBOL).build(null)
    );

    public static void registerBlocks() {
        Registry.register(Registries.BLOCK, SupernaturalMain.id("chalk_symbol"), CHALK_SYMBOL);
        Registry.register(Registries.BLOCK, SupernaturalMain.id("salt"), SALT);
    }

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, SupernaturalMain.id(path), blockEntityType);
    }

}

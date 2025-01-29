package net.jacobwasbeast.supernatural;

import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.jacobwasbeast.supernatural.blocks.HauntedBones;
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

public class ModBlocks implements BlockRegistryContainer {
    public static final Block CHALK_SYMBOL = new RitualChalk(FabricBlockSettings.of().noCollision().nonOpaque().sounds(BlockSoundGroup.GRAVEL).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().strength(1.0f).noCollision(), DyeColor.BLACK);
    public static final Block SALT = new Salt(FabricBlockSettings.of().noCollision().nonOpaque().sounds(BlockSoundGroup.GRAVEL).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().strength(1.0f).noCollision());
    public static final Block HAUNTED_BONES = new HauntedBones(FabricBlockSettings.of().noCollision().nonOpaque().sounds(BlockSoundGroup.GRAVEL).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().strength(1.0f).noCollision());
}

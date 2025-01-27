package net.jacobwasbeast.supernatural;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.jacobwasbeast.supernatural.blocks.entities.RitualChalkEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities implements AutoRegistryContainer<BlockEntityType<?>> {
    public static final BlockEntityType<RitualChalkEntity> RITUAL_CHALK_ENTITY = BlockEntityType.Builder.create(RitualChalkEntity::new, ModBlocks.CHALK_SYMBOL).build(null);

    @Override
    public Registry<BlockEntityType<?>> getRegistry() {
        return Registries.BLOCK_ENTITY_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<BlockEntityType<?>> getTargetFieldType() {
        return (Class<BlockEntityType<?>>) (Object) BlockEntityType.class;
    }
}

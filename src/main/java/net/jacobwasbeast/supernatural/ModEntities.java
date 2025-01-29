package net.jacobwasbeast.supernatural;

import io.wispforest.owo.registration.reflect.EntityRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.jacobwasbeast.supernatural.entities.*;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;


public class ModEntities implements EntityRegistryContainer {
    public static final EntityType<DemonEntity> DEMON = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DemonEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build();
    public static final EntityType<DemonVillager> DEMON_VILLAGER = FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DemonVillager::new).dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build();
    public static final EntityType<ColtBulletEntity> COLT_BULLET = FabricEntityTypeBuilder.<ColtBulletEntity>create(SpawnGroup.MISC, ColtBulletEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();
    public static final EntityType<GhostEntity> GHOST_ENTITY = FabricEntityTypeBuilder.<GhostEntity>create(SpawnGroup.MISC, GhostEntity::new).dimensions(EntityDimensions.fixed(0.5f, 2.5f)).build();
    public static final EntityType<FakeLightning> FAKELIGHTNING = FabricEntityTypeBuilder.<FakeLightning>create(SpawnGroup.MISC, FakeLightning::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();

    @Override
    public void afterFieldProcessing() {
        FabricDefaultAttributeRegistry.register(DEMON, DemonEntity.createDemonAttributes());
        FabricDefaultAttributeRegistry.register(DEMON_VILLAGER, DemonVillager.createDemonVillagerAttributes());
        FabricDefaultAttributeRegistry.register(GHOST_ENTITY, GhostEntity.createMobAttributes());
    }
}
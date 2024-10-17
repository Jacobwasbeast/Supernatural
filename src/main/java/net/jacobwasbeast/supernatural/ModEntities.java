package net.jacobwasbeast.supernatural;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.jacobwasbeast.supernatural.entities.ColtBulletEntity;
import net.jacobwasbeast.supernatural.entities.DemonEntity;
import net.jacobwasbeast.supernatural.entities.DemonVillager;
import net.jacobwasbeast.supernatural.entities.FakeLightning;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;


public class ModEntities {
    public static final EntityType<DemonEntity> DEMON = Registry.register(
            Registries.ENTITY_TYPE,
            SupernaturalMain.id("demon"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DemonEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build()
    );
    public static final EntityType<DemonVillager> DEMON_VILLAGER = Registry.register(
            Registries.ENTITY_TYPE,
            SupernaturalMain.id("demon_villager"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DemonVillager::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build()
    );

    public static final EntityType<ColtBulletEntity> COLT_BULLET = Registry.register(
            Registries.ENTITY_TYPE,
            SupernaturalMain.id("colt_bullet"),
            FabricEntityTypeBuilder.<ColtBulletEntity>create(SpawnGroup.MISC, ColtBulletEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build()
    );
    public static final EntityType<FakeLightning> FAKELIGHTNING = Registry.register(
            Registries.ENTITY_TYPE,
            SupernaturalMain.id("fakelightning"),
            FabricEntityTypeBuilder.<FakeLightning>create(SpawnGroup.MISC, FakeLightning::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build()
    );


    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(DEMON, DemonEntity.createDemonAttributes());
        FabricDefaultAttributeRegistry.register(DEMON_VILLAGER, DemonVillager.createDemonVillagerAttributes());
        // add custom spawn group for demons


    }
}
package net.jacobwasbeast.supernatural.client.models.blocks;

import net.jacobwasbeast.supernatural.SupernaturalMain;
import net.jacobwasbeast.supernatural.blocks.entities.HauntedBonesEntity;
import net.jacobwasbeast.supernatural.entities.GhostEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class HauntedBonesModel extends GeoModel<HauntedBonesEntity> {
    @Override
    public Identifier getModelResource(HauntedBonesEntity hauntedBonesEntity) {
        return SupernaturalMain.id("geo/bones.geo.json");
    }

    @Override
    public Identifier getTextureResource(HauntedBonesEntity hauntedBonesEntity) {
        return SupernaturalMain.id("textures/blocks/bones.png");
    }

    @Override
    public Identifier getAnimationResource(HauntedBonesEntity hauntedBonesEntity) {
        return null;
    }
}

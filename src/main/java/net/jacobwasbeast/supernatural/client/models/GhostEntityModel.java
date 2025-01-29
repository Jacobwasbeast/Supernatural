package net.jacobwasbeast.supernatural.client.models;

import net.jacobwasbeast.supernatural.SupernaturalMain;
import net.jacobwasbeast.supernatural.entities.GhostEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GhostEntityModel extends GeoModel<GhostEntity> {
    @Override
    public Identifier getModelResource(GhostEntity ghostEntity) {
        return SupernaturalMain.id("geo/ghost_entity.geo.json");
    }

    @Override
    public Identifier getTextureResource(GhostEntity ghostEntity) {
        return SupernaturalMain.id("textures/entities/ghost_entity.png");
    }

    @Override
    public Identifier getAnimationResource(GhostEntity ghostEntity) {
        return SupernaturalMain.id("animations/ghost_entity.animations.json");
    }
}

package net.jacobwasbeast.supernatural.client.renderer;

import net.jacobwasbeast.supernatural.client.models.GhostEntityModel;
import net.jacobwasbeast.supernatural.entities.GhostEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GhostEntityRenderer extends GeoEntityRenderer<GhostEntity> {
    public GhostEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GhostEntityModel());
    }
}

package net.jacobwasbeast.supernatural.client.renderer;

import net.jacobwasbeast.supernatural.entities.ColtBulletEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.util.Identifier;

public class ColtBulletEntityRenderer extends FlyingItemEntityRenderer<ColtBulletEntity> {
    public ColtBulletEntityRenderer(EntityRendererFactory.Context ctx, float scale, boolean lit) {
        super(ctx, scale, lit);
    }
}

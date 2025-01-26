package net.jacobwasbeast.supernatural;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.jacobwasbeast.supernatural.client.renderer.ColtBulletEntityRenderer;
import net.jacobwasbeast.supernatural.client.renderer.DemonEntityRenderer;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;

public class ModEntityRenderer {
    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(ModEntities.COLT_BULLET, ctx -> new ColtBulletEntityRenderer(ctx, 0.5f, false));
        EntityRendererRegistry.register(ModEntities.DEMON,DemonEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEMON_VILLAGER, VillagerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FAKELIGHTNING, LightningEntityRenderer::new);
    }
}

package net.jacobwasbeast.supernatural.client;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.jacobwasbeast.supernatural.ModBlocks;
import net.jacobwasbeast.supernatural.ModEntities;
import net.jacobwasbeast.supernatural.client.renderer.ColtBulletEntityRenderer;
import net.jacobwasbeast.supernatural.client.renderer.DemonEntityRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;

public class ModEntityRenderer {
    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(ModEntities.COLT_BULLET, ctx -> new ColtBulletEntityRenderer(ctx, 0.5f, false));
        EntityRendererRegistry.register(ModEntities.DEMON,DemonEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEMON_VILLAGER, VillagerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FAKELIGHTNING, LightningEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CHALK_SYMBOL, RenderLayer.getCutout());
        ColorProviderRegistry.BLOCK.register((state, world, pos, index) -> 2364706, ModBlocks.CHALK_SYMBOL);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SALT, RenderLayer.getCutout());
        ColorProviderRegistry.BLOCK.register((state, world, pos, index) -> 15264493, ModBlocks.SALT);
    }
}

package net.jacobwasbeast.supernatural.client.renderer;

import net.jacobwasbeast.supernatural.entities.DemonEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DemonEntityRenderer extends EntityRenderer<DemonEntity> {

    public DemonEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(DemonEntity entity, float yaw, float tickDelta, MatrixStack matrixStack, net.minecraft.client.render.VertexConsumerProvider vertexConsumerProvider, int light) {
        World world = entity.getWorld();

        // Create a smoke particle cloud around the demon
        for (int i = 0; i < 5; i++) {
            double offsetX = (world.random.nextDouble() - 0.1) * 0.1;
            double offsetY = (world.random.nextDouble()) * 0.05;
            double offsetZ = (world.random.nextDouble() - 0.1) * 0.1;

            Vec3d position = entity.getPos().add(offsetX, offsetY, offsetZ);
            // make the smoke particles move follow the demons velocity
            Vec3d velocity = entity.getVelocity().multiply(0.1, 0.1, 0.1);
            world.addParticle(ParticleTypes.SMOKE, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        }

        // No actual entity model render yet, just particles
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    @Override
    public Identifier getTexture(DemonEntity entity) {
        // Since we're not rendering a model, we don't need a texture
        return null;
    }
}

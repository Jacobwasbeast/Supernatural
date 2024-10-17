package net.jacobwasbeast.supernatural.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FakeLightning extends LightningEntity {
    public FakeLightning(EntityType<? extends LightningEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        // stay alive for 70 ticks
        this.age++;
        if (this.age == 2) {
            if (this.getWorld().isClient()) {
                this.getWorld()
                        .playSound(
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                                SoundCategory.WEATHER,
                                10000.0F,
                                0.8F + this.random.nextFloat() * 0.2F,
                                false
                        );
                this.getWorld()
                        .playSound(
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
                                SoundCategory.WEATHER,
                                2.0F,
                                0.5F + this.random.nextFloat() * 0.2F,
                                false
                        );
            } else {
            }
        }

        if (this.age >= 70) {
            this.remove(RemovalReason.UNLOADED_WITH_PLAYER);
        }
    }
}

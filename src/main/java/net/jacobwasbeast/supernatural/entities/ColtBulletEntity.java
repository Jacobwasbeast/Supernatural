package net.jacobwasbeast.supernatural.entities;

import net.jacobwasbeast.supernatural.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ColtBulletEntity extends PersistentProjectileEntity implements FlyingItemEntity {

    public ColtBulletEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public ColtBulletEntity(EntityType<? extends PersistentProjectileEntity> entityType, LivingEntity owner, World world) {
        super(entityType, owner, world);
    }

    @Override
    protected ItemStack asItemStack() {
        return new ItemStack(ModItems.COLTBULLET);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getEntityWorld().isClient && hitResult instanceof EntityHitResult) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            if (entityHitResult.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entityHitResult.getEntity();
                target.damage(getOwner().getDamageSources().magic(), 10.0F); // Deal damage
                this.remove(RemovalReason.KILLED); // Remove bullet after hitting
            }
            if (entityHitResult.getEntity() instanceof DemonEntity) {
                DemonEntity target = (DemonEntity) entityHitResult.getEntity();
                target.damage(getOwner().getDamageSources().magic(), 100000.0F); // Deal damage
                this.remove(RemovalReason.KILLED); // Remove bullet after hitting
            }
            if (entityHitResult.getEntity() instanceof DemonVillager) {
                DemonVillager target = (DemonVillager) entityHitResult.getEntity();
                target.damage(getOwner().getDamageSources().magic(), 100000.0F); // Deal damage
                this.remove(RemovalReason.KILLED); // Remove bullet after hitting
            }
        }
        else {
            this.remove(RemovalReason.KILLED); // Remove bullet after hitting
        }
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(ModItems.COLTBULLET);
    }
}

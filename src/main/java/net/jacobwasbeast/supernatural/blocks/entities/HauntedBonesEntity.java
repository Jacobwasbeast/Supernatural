package net.jacobwasbeast.supernatural.blocks.entities;

import net.jacobwasbeast.supernatural.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.UUID;

public class HauntedBonesEntity extends BlockEntity implements GeoAnimatable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public UUID entityID = null;
    public boolean isSalted = false;

    public HauntedBonesEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAUNTED_BONES_ENTITY, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (!Objects.equals(nbt.getString("supernatural:bones"), "")) {
            entityID = UUID.fromString(nbt.getString("supernatural:bones"));
        }
        else {
            entityID = null;
        }
        isSalted = nbt.getBoolean("supernatural:is_salted");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (entityID==null) {
            nbt.putString("supernatural:bones","");
        }
        else {
            nbt.putString("supernatural:bones",entityID.toString());
        }
        nbt.putBoolean("supernatural:is_salted",isSalted);
        super.writeNbt(nbt);
    }

    public void setEntityID(UUID uuid) {
        NbtCompound compound = new NbtCompound();
        compound.putString("supernatural:bones",uuid.toString());
        entityID = uuid;
        readNbt(compound);
    }

    public UUID getEntityID() {
        return entityID;
    }

    public void setIsSalted(boolean isSalted) {
        NbtCompound compound = new NbtCompound();
        compound.putBoolean("supernatural:is_salted",isSalted);
        this.isSalted = isSalted;
        readNbt(compound);
    }

    public boolean isSalted() {
        return isSalted;
    }
}

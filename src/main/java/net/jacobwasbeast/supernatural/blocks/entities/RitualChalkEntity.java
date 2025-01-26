package net.jacobwasbeast.supernatural.blocks.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import static net.jacobwasbeast.supernatural.ModBlocks.CHALK_ENTITY;

public class RitualChalkEntity extends BlockEntity {
    public String ritual = "NULL";

    public RitualChalkEntity(BlockPos pos, BlockState state) {
        super(CHALK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        ritual = nbt.getString("supernatural:ritual");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putString("supernatural:ritual",ritual);
        super.writeNbt(nbt);
    }
}

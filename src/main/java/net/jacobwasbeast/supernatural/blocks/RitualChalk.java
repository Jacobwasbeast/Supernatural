package net.jacobwasbeast.supernatural.blocks;

import de.dafuqs.chalk.common.blocks.ChalkMarkBlock;
import net.jacobwasbeast.supernatural.ModBlocks;
import net.jacobwasbeast.supernatural.api.Ritual;
import net.jacobwasbeast.supernatural.api.RitualManager;
import net.jacobwasbeast.supernatural.blocks.entities.RitualChalkEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class RitualChalk extends ChalkMarkBlock implements BlockEntityProvider {
    public RitualChalk(Settings settings, DyeColor dyeColor) {
        super(settings, dyeColor);
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RitualChalkEntity(pos,state);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity!=null) {
            RitualChalkEntity chalk = (RitualChalkEntity) entity;
            String ritualString = chalk.ritual;
            Ritual foundRitual = null;
            for (Ritual ritual : RitualManager.getInstance().rituals.values()) {
                if (ritual.reference.getPath().equals(ritualString)) {
                    foundRitual = ritual;
                    break;
                }
            }
            if (foundRitual!=null) {
                int radius = (int) foundRitual.radius/2;
                for (int x = -radius; x<radius; x++) {
                    for (int z = -radius; z<radius; z++) {
                        BlockPos nPos = pos.add(x,0,z);
                        BlockState stateAt = world.getBlockState(nPos);
                        if (stateAt.isOf(ModBlocks.CHALK_SYMBOL)) {
                            RitualChalkEntity entityAt = (RitualChalkEntity) world.getBlockEntity(nPos);
                            if (foundRitual.reference.getPath().equals(entityAt.ritual)) {
                                radius++;
                                world.breakBlock(nPos,false);
                                world.markDirty(nPos);
                            }
                        }
                    }
                }
            }
        }
    }
}

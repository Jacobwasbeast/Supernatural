package net.jacobwasbeast.supernatural.blocks;

import net.jacobwasbeast.supernatural.items.Chalk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ChalkSymbolBlock extends Block {
    // A property to toggle if the symbol is active (used in a ritual)
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    public ChalkSymbolBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(ACTIVE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE); // Add the active property
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // Create a flat, square shape for the chalk symbol
        return Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // Check if the player has chalk in hand
        ItemStack heldItem = player.getStackInHand(hand);

        if (!world.isClient && heldItem.getItem() instanceof Chalk) {
            // Toggle the active state of the chalk symbol when interacted with chalk
            boolean isActive = state.get(ACTIVE);
            world.setBlockState(pos, state.with(ACTIVE, !isActive), 3);

            // Play a sound when the symbol is activated/deactivated
            world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);

        // Check if the block has been destroyed or if a ritual is complete, for example
        if (!world.isClient && world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.with(ACTIVE, true), 3);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);

        // Play a sound when the symbol is erased
        world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }
}

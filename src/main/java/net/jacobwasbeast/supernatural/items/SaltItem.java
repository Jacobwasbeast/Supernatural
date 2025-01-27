package net.jacobwasbeast.supernatural.items;

import de.dafuqs.chalk.client.config.ConfigHelper;
import de.dafuqs.chalk.common.ChalkRegistry;
import de.dafuqs.chalk.common.items.ChalkItem;
import net.jacobwasbeast.supernatural.ModBlocks;
import net.jacobwasbeast.supernatural.blocks.Salt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SaltItem extends Item {
    protected DyeColor dyeColor;

    public SaltItem(Settings settings, DyeColor dyeColor) {
        super(settings);
        this.dyeColor = dyeColor;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState clickedBlockState = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        Direction clickedFace = context.getSide();
        BlockPos markPosition = pos.offset(clickedFace);
        if (world.isAir(markPosition) || world.getBlockState(markPosition).getBlock() instanceof Salt) {
            if (clickedBlockState.getBlock() instanceof Salt) {
                markPosition = pos;
                world.removeBlock(pos, false);
            } else {
                if (player != null && !Block.isFaceFullSquare(clickedBlockState.getCollisionShape(world, pos, ShapeContext.of(player)), clickedFace)) {
                    return ActionResult.PASS;
                }

                if (!world.isAir(markPosition) && world.getBlockState(markPosition).getBlock() instanceof Salt || stack.getItem() != this) {
                    return ActionResult.PASS;
                }
            }

            if (world.isClient) {
                Random r = new Random();
                if ((Boolean) ConfigHelper.getConfig("emit_particles")) {
                    world.addParticle(
                            ParticleTypes.CLOUD,
                            (double)markPosition.getX() + 0.5 * ((double)r.nextFloat() + 0.4),
                            (double)markPosition.getY() + 0.65,
                            (double)markPosition.getZ() + 0.5 * ((double)r.nextFloat() + 0.4),
                            0.0,
                            0.005,
                            0.0
                    );
                }

                return ActionResult.SUCCESS;
            }

            BlockState blockState = ModBlocks.SALT
                    .getDefaultState();
            if (world.setBlockState(markPosition, blockState, 3)) {
                if (player != null && !player.isCreative()) {
                    if (stack.getDamage() >= stack.getMaxDamage()) {
                        world.playSound(null, markPosition, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    }

                    stack.damage(1, player, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                }

                world.playSound(
                        null, markPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 0.6F, world.random.nextFloat() * 0.2F + 0.8F
                );
                return ActionResult.CONSUME;
            }
        }

        return ActionResult.FAIL;
    }
}

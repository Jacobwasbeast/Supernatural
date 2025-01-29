package net.jacobwasbeast.supernatural.blocks;

import net.jacobwasbeast.supernatural.ModBlockEntities;
import net.jacobwasbeast.supernatural.ModEntities;
import net.jacobwasbeast.supernatural.blocks.entities.HauntedBonesEntity;
import net.jacobwasbeast.supernatural.entities.GhostEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class HauntedBones extends BlockWithEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HauntedBones(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HauntedBonesEntity(pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        // Register animation controllers here if needed
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        burnBones(world,pos);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        // Create and spawn a ghost entity when the block is placed
        GhostEntity ghostEntity = ModEntities.GHOST_ENTITY.create(world);
        if (ghostEntity != null) {
            ghostEntity.setPosition(pos.toCenterPos());
            ghostEntity.setRestingSpot(pos);
            world.spawnEntity(ghostEntity);

            // Link the ghost entity to the HauntedBones block entity
            if (world.getBlockEntity(pos) instanceof HauntedBonesEntity bones) {
                bones.setEntityID(ghostEntity.getUuid());
            }
        }
    }

    public void burnBones(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof HauntedBonesEntity bones && bones.getEntityID() != null) {
            // Search for associated ghost entities within a 30-block radius
            Box searchArea = new Box(pos).expand(30);
            List<GhostEntity> ghostEntities = world.getEntitiesByClass(
                    GhostEntity.class,
                    searchArea,
                    entity -> bones.getEntityID().equals(entity.getUuid())
            );

            ghostEntities.forEach(GhostEntity::burnBones);
        }
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).isOf(Items.FLINT_AND_STEEL)) {
            HauntedBonesEntity bones = (HauntedBonesEntity)world.getBlockEntity(pos);
            if (bones.isSalted()) {
                burnBones(world,pos);
                world.playSound(
                        null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.6F, world.random.nextFloat() * 0.2F + 0.8F
                );
                return ActionResult.CONSUME;
            }
            else {
                player.sendMessage(Text.literal("To burn bones, you have to salt them first."),true);
                return ActionResult.FAIL;
            }
        }
        else {
            return ActionResult.FAIL;
        }
    }
}
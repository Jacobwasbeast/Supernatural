package net.jacobwasbeast.supernatural.entities;

import net.jacobwasbeast.supernatural.ModBlocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Block;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhostEntity extends PathAwareEntity implements GeoEntity {
    protected static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("animation.idle");
    protected static final RawAnimation BURN_ANIM = RawAnimation.begin().thenPlayAndHold("animation.burn");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final TrackedData<Boolean> BURNING = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<BlockPos> RESTING_SPOT = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    private boolean isAggressive = false;
    private int teleportCooldown = 0;
    private int huntTimer = 0;
    private int invisibleTimer = 0;
    private int burnTimer = 0;
    private int forcedVisibleTimer = 0;
    private int huntVisibilityTimer = 0;
    private boolean isHuntVisiblePhase = true;

    public GhostEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
        this.setPathfindingPenalty(PathNodeType.LAVA, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
        this.experiencePoints = 10;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new HauntPlayerGoal(this, 0.5D, 20, 15.0F));
        this.goalSelector.add(3, new RandomFlyGoal(this, 0.5D));
        this.goalSelector.add(4, new ReturnToRestingSpotGoal(this, 0.5D));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BURNING, false);
        this.dataTracker.startTracking(RESTING_SPOT, BlockPos.ORIGIN);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    public boolean isBurning() {
        return this.dataTracker.get(BURNING);
    }

    public void setBurning(boolean burning) {
        this.dataTracker.set(BURNING, burning);
    }

    public BlockPos getRestingSpot() {
        return this.dataTracker.get(RESTING_SPOT);
    }

    public void setRestingSpot(BlockPos spot) {
        this.dataTracker.set(RESTING_SPOT, spot);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean validDamage = false;

        if (source.getSource() instanceof PlayerEntity player) {
            ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);
            if (heldItem.getItem() == Items.IRON_SWORD ||
                    heldItem.getItem() == Items.IRON_AXE ||
                    heldItem.getItem() == Items.IRON_PICKAXE ||
                    heldItem.getItem() == Items.IRON_SHOVEL ||
                    heldItem.getItem() == Items.IRON_HOE) {
                validDamage = true;
            }
        }
        else if (source.getSource() instanceof ColtBulletEntity) {
            validDamage = true;
        }

        if (validDamage) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2));
            this.teleportAwayFromPlayer();
            return false;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            if (this.getRestingSpot().equals(BlockPos.ORIGIN)) {
                this.setRestingSpot(this.getBlockPos());
                burnBones();
            }

            if (this.isBurning()) {
                if (this.burnTimer > 0) {
                    this.setOnFire(true);
                    this.burnTimer--;
                    this.setInvisible(false);
                    this.forcedVisibleTimer = this.burnTimer;
                    this.getNavigation().stop();
                } else {
                    this.remove(RemovalReason.DISCARDED);
                }
            }
            else {
                if (this.forcedVisibleTimer > 0) {
                    this.forcedVisibleTimer--;
                    this.setInvisible(false);
                } else {
                    if (this.isAggressive) {
                        if (this.huntTimer > 0) {
                            this.huntTimer--;
                            this.huntVisibilityTimer--;

                            if (this.huntVisibilityTimer <= 0) {
                                this.isHuntVisiblePhase = !this.isHuntVisiblePhase;
                                this.huntVisibilityTimer = this.isHuntVisiblePhase ?
                                        this.random.nextInt(40) + 80 :
                                        this.random.nextInt(40) + 20;
                            }
                            this.setInvisible(!this.isHuntVisiblePhase);
                        } else {
                            this.isAggressive = false;
                            this.invisibleTimer = this.random.nextInt(600) + 300;
                            this.setInvisible(true);
                        }
                    } else {
                        if (this.invisibleTimer > 0) {
                            this.invisibleTimer--;
                            this.setInvisible(true);
                        } else {
                            this.isAggressive = true;
                            this.huntTimer = 1200;
                            this.isHuntVisiblePhase = true;
                            this.huntVisibilityTimer = this.random.nextInt(40) + 80;
                        }
                    }
                }
            }

            if (this.teleportCooldown > 0) this.teleportCooldown--;

            if (!this.isAggressive && !this.getRestingSpot().equals(BlockPos.ORIGIN) &&
                    this.squaredDistanceTo(Vec3d.of(this.getRestingSpot())) > 256.0D) {
                this.teleportToRestingSpot();
            }

            if (isSaltNearby(this.getBlockPos())) {
                this.teleportAwayFromSalt();
            }
        }
    }

    private boolean isSaltNearby(BlockPos pos) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (this.getWorld().getBlockState(pos.add(x, 0, z)).getBlock() == ModBlocks.SALT) {
                    return true;
                }
            }
        }
        return false;
    }

    private void teleportAwayFromSalt() {
        BlockPos safePos = findSafePosition();
        if (safePos != null) {
            this.ghostTeleport(
                    safePos.getX() + 0.5,
                    safePos.getY(),
                    safePos.getZ() + 0.5
            );
        }
    }

    private BlockPos findSafePosition() {
        for (int i = 0; i < 10; i++) {
            BlockPos randomPos = this.getBlockPos().add(
                    this.random.nextInt(32) - 16,
                    this.random.nextInt(16) - 8,
                    this.random.nextInt(32) - 16
            );
            if (!isSaltNearby(randomPos)) return randomPos;
        }
        return null;
    }

    public void teleportToRestingSpot() {
        if (!this.getRestingSpot().equals(BlockPos.ORIGIN) && this.teleportCooldown <= 0) {
            this.ghostTeleport(
                    this.getRestingSpot().getX() + 0.5,
                    this.getRestingSpot().getY(),
                    this.getRestingSpot().getZ() + 0.5
            );
            this.teleportCooldown = 100;
        }
    }

    public void ghostTeleport(double x, double y, double z) {
        BlockPos targetPos = new BlockPos((int)x, (int)y, (int)z);
        if (!isSaltNearby(targetPos)) {
            this.requestTeleport(x, y, z);
            this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            this.forcedVisibleTimer = 20;
        }
    }

    private void teleportAwayFromPlayer() {
        BlockPos safePos = findSafePosition();
        if (safePos != null) {
            this.ghostTeleport(
                    safePos.getX() + 0.5,
                    safePos.getY(),
                    safePos.getZ() + 0.5
            );
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "Flying", 0, this::flyAnimController));
        controllerRegistrar.add(new AnimationController<>(this, "Burning", 0, this::burnAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends GhostEntity> PlayState flyAnimController(final AnimationState<E> event) {
        return event.setAndContinue(FLY_ANIM);
    }

    protected <E extends GhostEntity> PlayState burnAnimController(final AnimationState<E> event) {
        return this.isBurning() ? event.setAndContinue(BURN_ANIM) : PlayState.STOP;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!this.getWorld().getBlockState(this.getRestingSpot()).isOf(ModBlocks.HAUNTED_BONES)) {
            burnBones();
        }
    }

    public void burnBones() {
        if (!this.isBurning()) {
            this.setBurning(true);
            this.burnTimer = 60;
            this.setInvisible(false);
            this.forcedVisibleTimer = 60;
            this.teleportToRestingSpot();
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1000, 100, false, false));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 1000, 100, false, false));
            this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 1.0F, 1.0F);
        }
    }

    private static class HauntPlayerGoal extends Goal {
        private final GhostEntity ghost;
        private final double speed;
        private final int chance;
        private final float radius;
        private PlayerEntity targetPlayer;

        public HauntPlayerGoal(GhostEntity ghost, double speed, int chance, float radius) {
            this.ghost = ghost;
            this.speed = speed;
            this.chance = chance;
            this.radius = radius;
        }

        @Override
        public boolean canStart() {
            if (this.ghost.random.nextInt(this.chance) != 0) return false;
            this.targetPlayer = this.ghost.getWorld().getClosestPlayer(
                    this.ghost.getX(), this.ghost.getY(), this.ghost.getZ(),
                    this.radius, true
            );
            return this.targetPlayer != null && !this.ghost.isBurning();
        }

        @Override
        public void start() {
            this.ghost.getNavigation().startMovingTo(this.targetPlayer, this.speed);
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
        }

        @Override
        public boolean shouldContinue() {
            return this.targetPlayer != null &&
                    this.targetPlayer.isAlive() &&
                    this.ghost.squaredDistanceTo(this.targetPlayer) < 256.0D &&
                    !this.ghost.isBurning();
        }

        @Override
        public void tick() {
            if (this.targetPlayer != null) {
                this.targetPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
                this.targetPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0));

                if (this.ghost.squaredDistanceTo(this.targetPlayer) < 4.0D) {
                    this.targetPlayer.damage(this.targetPlayer.getDamageSources().mobAttack(this.ghost), 2.0F);
                    this.ghost.forcedVisibleTimer = 20;
                }

                if (this.ghost.teleportCooldown <= 0 && this.ghost.random.nextInt(20) == 0) {
                    double x = this.targetPlayer.getX() + (this.ghost.random.nextDouble() - 0.5) * 10.0;
                    double y = this.targetPlayer.getY() + this.ghost.random.nextDouble() * 5.0;
                    double z = this.targetPlayer.getZ() + (this.ghost.random.nextDouble() - 0.5) * 10.0;
                    BlockPos targetPos = new BlockPos((int)x, (int)y, (int)z);
                    if (!this.ghost.isSaltNearby(targetPos)) {
                        this.ghost.ghostTeleport(x, y, z);
                    }
                }
            }
        }
    }

    private static class ReturnToRestingSpotGoal extends Goal {
        private final GhostEntity ghost;
        private final double speed;

        public ReturnToRestingSpotGoal(GhostEntity ghost, double speed) {
            this.ghost = ghost;
            this.speed = speed;
        }

        @Override
        public boolean canStart() {
            return !this.ghost.isAggressive &&
                    !this.ghost.getRestingSpot().equals(BlockPos.ORIGIN) &&
                    this.ghost.squaredDistanceTo(Vec3d.of(this.ghost.getRestingSpot())) > 64.0D;
        }

        @Override
        public void start() {
            this.ghost.getNavigation().startMovingTo(
                    this.ghost.getRestingSpot().getX(),
                    this.ghost.getRestingSpot().getY(),
                    this.ghost.getRestingSpot().getZ(),
                    this.speed
            );
        }

        @Override
        public boolean shouldContinue() {
            return !this.ghost.getNavigation().isIdle() &&
                    this.ghost.squaredDistanceTo(Vec3d.of(this.ghost.getRestingSpot())) > 16.0D;
        }
    }

    private static class RandomFlyGoal extends Goal {
        private final GhostEntity ghost;
        private final double speed;

        public RandomFlyGoal(GhostEntity ghost, double speed) {
            this.ghost = ghost;
            this.speed = speed;
        }

        @Override
        public boolean canStart() {
            return this.ghost.getNavigation().isIdle() &&
                    this.ghost.random.nextInt(10) == 0 &&
                    !this.ghost.isBurning();
        }

        @Override
        public void start() {
            BlockPos randomPos = this.ghost.getRestingSpot().add(
                    this.ghost.random.nextInt(32) - 16,
                    this.ghost.random.nextInt(16) - 8,
                    this.ghost.random.nextInt(32) - 16
            );
            this.ghost.getNavigation().startMovingTo(randomPos.getX(), randomPos.getY(), randomPos.getZ(), this.speed);
        }
    }
}
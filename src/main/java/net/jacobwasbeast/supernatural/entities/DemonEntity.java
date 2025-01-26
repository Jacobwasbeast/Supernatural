package net.jacobwasbeast.supernatural.entities;

import net.jacobwasbeast.supernatural.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * DemonEntity represents a hostile supernatural demon that spends most of its time in the air.
 */
public class DemonEntity extends HostileEntity {
    // Tracked data for demon size
    private static final TrackedData<Integer> SIZE = DataTracker.registerData(DemonEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // Fields
    private Vec3d targetPosition = Vec3d.ZERO;
    private BlockPos circlingCenter = BlockPos.ORIGIN;
    private DemonMovementType movementType = DemonMovementType.PATROL;
    public boolean runAway = false;

    public DemonEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 5;
        this.moveControl = new DemonMoveControl(this);
        this.lookControl = new DemonLookControl(this);
        this.setNoGravity(true);  // Disable gravity for stable flight
    }

    /**
     * Registers the default attributes for the DemonEntity.
     *
     * @return The attribute container builder.
     */
    public static DefaultAttributeContainer.Builder createDemonAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 4000.0)  // Custom health
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)  // Custom attack damage
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5);  // Increased movement speed for better patrolling
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SIZE, 0);
    }

    /**
     * Sets the demon's size.
     *
     * @param size The size to set, clamped between 0 and 64.
     */
    public void setDemonSize(int size) {
        this.dataTracker.set(SIZE, MathHelper.clamp(size, 0, 64));
    }

    private void onSizeChanged() {
        this.calculateDimensions();
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(6 + this.getDemonSize());
    }

    /**
     * Retrieves the demon's size.
     *
     * @return The demon's size.
     */
    public int getDemonSize() {
        return this.dataTracker.get(SIZE);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (SIZE.equals(data)) {
            this.onSizeChanged();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    protected BodyControl createBodyControl() {
        return new DemonBodyControl(this);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        // Primary Airborne Movement Goals
        this.goalSelector.add(5, new AirPatrolGoal());
        this.goalSelector.add(6, new StartAttackGoal());
        this.goalSelector.add(7, new SwoopMovementGoal());
        this.goalSelector.add(8, new CircleMovementGoal());

        // Targeting Goals
        this.targetSelector.add(4, new FindTargetGoal());

        // Secondary AI Goals
        this.goalSelector.add(3, new RunAwayGoal(this));
        this.goalSelector.add(2, new PossessVillagerGoal(this));
        this.goalSelector.add(1, new SeekVillagerGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            // Client-side effects like particles and sounds can be added here if desired
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        // Neutralize Y velocity to maintain stable flight
        Vec3d velocity = this.getVelocity();
        this.setVelocity(velocity.x, 0.0, velocity.z);  // Prevent unintended vertical movement
    }

    // Collision handling
    @Override
    public boolean isOnGround() {
        return false;  // Always considered as flying
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;  // No collision with other entities
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        // Prevent any block collision effects
    }

    @Override
    public boolean isCollidable() {
        return false;  // Disable collidability
    }

    @Override
    public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        return true;  // Allow movement through any position
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        return false;  // Optionally, disable handling attacks
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        int size = this.getDemonSize();
        EntityDimensions dimensions = super.getDimensions(pose);
        float scale = (dimensions.width + 0.2F * size) / dimensions.width;
        return dimensions.scaled(scale);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("AX")) {
            this.circlingCenter = new BlockPos(nbt.getInt("AX"), nbt.getInt("AY"), nbt.getInt("AZ"));
        }
        this.setDemonSize(nbt.getInt("Size"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("AX", this.circlingCenter.getX());
        nbt.putInt("AY", this.circlingCenter.getY());
        nbt.putInt("AZ", this.circlingCenter.getZ());
        nbt.putInt("Size", this.getDemonSize());
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;  // Always render
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;  // Use appropriate sound
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PHANTOM_HURT;  // Use appropriate sound
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PHANTOM_DEATH;  // Use appropriate sound
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;  // Adjust as needed
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return true;
    }

    @Override
    public double getMountedHeightOffset() {
        return (double) this.getStandingEyeHeight();
    }

    /**
     * Custom Body Control to align head and body
     */
    static class DemonBodyControl extends BodyControl {
        private final DemonEntity demon;

        public DemonBodyControl(DemonEntity demon) {
            super(demon);
            this.demon = demon;
        }

        @Override
        public void tick() {
            demon.headYaw = demon.bodyYaw;
            demon.bodyYaw = demon.getYaw();
        }
    }

    /**
     * Custom Look Control (can be enhanced as needed)
     */
    static class DemonLookControl extends LookControl {
        private final DemonEntity demon;

        public DemonLookControl(DemonEntity demon) {
            super(demon);
            this.demon = demon;
        }

        @Override
        public void tick() {
            // Implement custom look behavior if desired
            // For simplicity, keeping it static
        }
    }

    /**
     * Custom Move Control to handle 3D movement
     */
    static class DemonMoveControl extends MoveControl {
        private final DemonEntity demon;
        private float targetSpeed = 0.5F;  // Increased speed for better patrolling

        public DemonMoveControl(DemonEntity demon) {
            super(demon);
            this.demon = demon;
        }

        @Override
        public void tick() {
            if (demon.targetPosition == null) {
                return;
            }

            double dx = demon.targetPosition.x - demon.getX();
            double dy = demon.targetPosition.y - demon.getY();
            double dz = demon.targetPosition.z - demon.getZ();
            double distanceXZ = Math.sqrt(dx * dx + dz * dz);

            if (distanceXZ > 1.0E-5F) {
                double scale = 1.0 - Math.abs(dy * 0.7F) / distanceXZ;
                dx *= scale;
                dz *= scale;
                distanceXZ = Math.sqrt(dx * dx + dz * dz);
                double distanceXYZ = Math.sqrt(dx * dx + dz * dz + dy * dy);
                float yaw = demon.getYaw();
                float targetYaw = (float) MathHelper.atan2(dz, dx) * (180F / (float) Math.PI);
                demon.setYaw(MathHelper.wrapDegrees(targetYaw - 90.0F));
                demon.bodyYaw = demon.getYaw();

                if (MathHelper.angleBetween(yaw, demon.getYaw()) < 3.0F) {
                    this.targetSpeed = MathHelper.stepTowards(this.targetSpeed, 1.8F, 0.005F * (1.8F / this.targetSpeed));
                } else {
                    this.targetSpeed = MathHelper.stepTowards(this.targetSpeed, 0.3F, 0.025F);
                }

                float pitch = (float) (-(MathHelper.atan2(-dy, distanceXZ) * 180.0F / Math.PI));
                demon.setPitch(pitch);

                float adjustedYaw = demon.getYaw() + 90.0F;
                double vx = this.targetSpeed * MathHelper.cos(adjustedYaw * ((float) Math.PI / 180.0F)) * Math.abs(dx / distanceXYZ);
                double vz = this.targetSpeed * MathHelper.sin(adjustedYaw * ((float) Math.PI / 180.0F)) * Math.abs(dz / distanceXYZ);
                double vy = this.targetSpeed * MathHelper.sin(pitch * ((float) Math.PI / 180.0F)) * Math.abs(dy / distanceXYZ);

                Vec3d currentVelocity = demon.getVelocity();
                Vec3d desiredVelocity = new Vec3d(vx, vy, vz);
                demon.setVelocity(currentVelocity.add(desiredVelocity.subtract(currentVelocity).multiply(0.2)));
            }
        }
    }

    /**
     * Enumeration for Demon Movement Types
     */
    enum DemonMovementType {
        PATROL,  // Primary patrolling behavior
        SWOOP,   // Swooping towards targets
        RUNAWAY  // Fleeing behavior
    }

    /**
     * Air Patrol Goal: Makes the demon patrol the skies independently.
     *
     * This goal ensures that the demon spends most of its time in the air,
     * patrolling random locations and maintaining altitude.
     */
    class AirPatrolGoal extends Goal {
        private int patrolCooldown = 0;

        public AirPatrolGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            // Can start if demon is not currently engaged in attacking or possessing
            return !DemonEntity.this.runAway && DemonEntity.this.getTarget() == null && DemonEntity.this.movementType == DemonMovementType.PATROL;
        }

        @Override
        public void start() {
            setNewPatrolTarget();
        }

        @Override
        public void tick() {
            DemonEntity demon = DemonEntity.this;

            // Periodically change patrol targets to different locations in the air
            if (patrolCooldown <= 0) {
                setNewPatrolTarget();
                patrolCooldown = demon.random.nextInt(100) + 100;  // Cooldown between 100-200 ticks
            } else {
                patrolCooldown--;
            }

            // Ensure the demon maintains a certain altitude
            double desiredY = demon.targetPosition.y;
            desiredY = MathHelper.clamp(desiredY, demon.getEntityWorld().getSeaLevel() + 30, demon.getEntityWorld().getHeight() - 10);
            demon.targetPosition = new Vec3d(demon.targetPosition.x, desiredY, demon.targetPosition.z);
        }

        private void setNewPatrolTarget() {
            DemonEntity demon = DemonEntity.this;
            Random random = demon.getRandom();

            double newX = demon.getX() + (random.nextDouble() * 400 - 200);  // Increased patrol range
            double newZ = demon.getZ() + (random.nextDouble() * 400 - 200);
            double newY = demon.getEntityWorld().getSeaLevel() + 50 + random.nextDouble() * 30;  // Higher altitude patrol
            newY = Math.min(newY, demon.getEntityWorld().getHeight() - 10);  // Prevent going too high

            demon.targetPosition = new Vec3d(newX, newY, newZ);
            demon.movementType = DemonMovementType.PATROL;
        }

        @Override
        public boolean shouldContinue() {
            return DemonEntity.this.getTarget() == null && !DemonEntity.this.runAway && DemonEntity.this.movementType == DemonMovementType.PATROL;
        }
    }

    /**
     * Circle Movement Goal: Circles around a designated center point.
     *
     * This goal complements the AirPatrolGoal by allowing the demon to circle around specific points when needed.
     */
    class CircleMovementGoal extends Goal {
        private float angle;
        private float radius;
        private float yOffset;
        private float circlingDirection;

        public CircleMovementGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            // Can start if the demon is in SWOOP movement type
            return DemonEntity.this.movementType == DemonMovementType.SWOOP;
        }

        @Override
        public void start() {
            DemonEntity.this.movementType = DemonMovementType.SWOOP;
            this.radius = 30.0F + DemonEntity.this.random.nextFloat() * 20.0F;  // Larger radius for broader swoop
            this.yOffset = 0.0F;  // Maintain consistent altitude
            this.circlingDirection = DemonEntity.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.adjustDirection();
        }

        @Override
        public void tick() {
            DemonEntity demon = DemonEntity.this;

            // Adjust direction less frequently for smoother movement
            if (demon.random.nextInt(this.getTickCount(800)) == 0) {
                this.angle = demon.random.nextFloat() * 2.0F * (float) Math.PI;
                this.adjustDirection();
            }

            // Ensure the demon maintains altitude
            demon.targetPosition = new Vec3d(demon.circlingCenter.getX(), demon.circlingCenter.getY(), demon.circlingCenter.getZ())
                    .add(
                            (double) (this.radius * MathHelper.cos(this.angle)),
                            (double) this.yOffset,
                            (double) (this.radius * MathHelper.sin(this.angle))
                    );
        }

        private void adjustDirection() {
            DemonEntity demon = DemonEntity.this;
            if (BlockPos.ORIGIN.equals(demon.circlingCenter)) {
                demon.circlingCenter = demon.getBlockPos();
            }

            this.angle += this.circlingDirection * 15.0F * ((float) Math.PI / 180.0F);
        }

        @Override
        public boolean shouldContinue() {
            return DemonEntity.this.movementType == DemonMovementType.SWOOP;
        }
    }

    /**
     * Movement Goal: Swoop towards target
     */
    class SwoopMovementGoal extends Goal {
        private static final int CAT_CHECK_INTERVAL = 20;
        private boolean catsNearby;
        private int nextCatCheckAge;

        public SwoopMovementGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return DemonEntity.this.getTarget() != null && DemonEntity.this.movementType == DemonMovementType.SWOOP;
        }

        @Override
        public boolean shouldContinue() {
            DemonEntity demon = DemonEntity.this;
            LivingEntity target = demon.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }

            if (target instanceof PlayerEntity player && (player.isSpectator() || player.isCreative())) {
                return false;
            }

            if (!this.canStart()) {
                return false;
            }

            if (demon.age > this.nextCatCheckAge) {
                this.nextCatCheckAge = demon.age + CAT_CHECK_INTERVAL;
                List<VillagerEntity> nearbyVillagers = demon.getWorld()
                        .getEntitiesByClass(VillagerEntity.class, demon.getBoundingBox().expand(16.0), EntityPredicates.VALID_ENTITY);
                // Safely remove DemonVillagers
                nearbyVillagers.removeIf(villager -> villager instanceof DemonVillager);
                for (VillagerEntity villager : nearbyVillagers) {
                    villager.damage(getDamageSources().magic(), 2.0F);  // Example interaction using MAGIC damage source
                }

                this.catsNearby = !nearbyVillagers.isEmpty();
            }

            return !this.catsNearby;
        }

        @Override
        public void start() {
            // Optional: Implement any start logic
        }

        @Override
        public void stop() {
            DemonEntity.this.setTarget(null);
            DemonEntity.this.movementType = DemonMovementType.PATROL;
        }

        @Override
        public void tick() {
            DemonEntity demon = DemonEntity.this;
            LivingEntity target = demon.getTarget();
            if (target != null) {
                demon.targetPosition = new Vec3d(target.getX(), target.getBodyY(0.5), target.getZ());
                if (demon.getBoundingBox().expand(0.2F).intersects(target.getBoundingBox())) {
                    demon.tryAttack(target);
                    demon.movementType = DemonMovementType.PATROL;
                    if (!demon.isSilent()) {
                        demon.getWorld().syncWorldEvent(1039, demon.getBlockPos(), 0);
                    }
                } else if (demon.horizontalCollision || demon.hurtTime > 0) {
                    demon.movementType = DemonMovementType.PATROL;
                }
            }
        }
    }

    /**
     * Start Attack Goal: Switch between patrolling and swooping
     */
    class StartAttackGoal extends Goal {
        private int cooldown;

        public StartAttackGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = DemonEntity.this.getTarget();
            return target != null && DemonEntity.this.isTarget(target, TargetPredicate.DEFAULT);
        }

        @Override
        public void start() {
            this.cooldown = this.getTickCount(10);
            DemonEntity.this.movementType = DemonMovementType.PATROL;
            this.startSwoop();
        }

        @Override
        public void stop() {
            DemonEntity demon = DemonEntity.this;
            demon.circlingCenter = demon.getWorld()
                    .getTopPosition(Type.MOTION_BLOCKING, demon.circlingCenter)
                    .up(10 + demon.random.nextInt(20));
        }

        @Override
        public void tick() {
            DemonEntity demon = DemonEntity.this;
            if (demon.movementType == DemonMovementType.PATROL) {
                this.cooldown--;
                if (this.cooldown <= 0) {
                    demon.movementType = DemonMovementType.SWOOP;
                    this.startSwoop();
                    this.cooldown = this.getTickCount((8 + demon.random.nextInt(4)) * 20);
                    demon.playSound(SoundEvents.ENTITY_PHANTOM_SWOOP, 10.0F, 0.95F + demon.random.nextFloat() * 0.1F);
                }
            }
        }

        /**
         * Initiates the swoop behavior towards the target.
         */
        private void startSwoop() {
            DemonEntity demon = DemonEntity.this;
            LivingEntity target = demon.getTarget();
            if (target != null) {
                demon.circlingCenter = new BlockPos(
                        (int) target.getX(),
                        (int) (target.getY() + 20 + demon.random.nextInt(20)),
                        (int) target.getZ()
                );
                if (demon.circlingCenter.getY() < demon.getWorld().getSeaLevel()) {
                    demon.circlingCenter = new BlockPos(
                            demon.circlingCenter.getX(),
                            demon.getWorld().getSeaLevel() + 1,
                            demon.circlingCenter.getZ()
                    );
                }
            }
        }
    }

    /**
     * Find Target Goal: Find players to target
     */
    class FindTargetGoal extends Goal {
        private final TargetPredicate TARGET_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(128.0);  // Increased range
        private int delay = toGoalTicks(20);

        public FindTargetGoal() {
            this.setControls(EnumSet.noneOf(Control.class));
        }

        @Override
        public boolean canStart() {
            if (this.delay > 0) {
                this.delay--;
                return false;
            } else {
                this.delay = toGoalTicks(60);
                List<PlayerEntity> players = DemonEntity.this.getWorld()
                        .getPlayers(this.TARGET_PREDICATE, DemonEntity.this, DemonEntity.this.getBoundingBox().expand(200.0, 100.0, 200.0));  // Expanded range
                if (!players.isEmpty()) {
                    players.sort(Comparator.comparing(Entity::getY).reversed());

                    for (PlayerEntity player : players) {
                        if (DemonEntity.this.isTarget(player, TargetPredicate.DEFAULT)) {
                            DemonEntity.this.setTarget(player);
                            return true;
                        }
                    }
                }
                return false;
            }
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = DemonEntity.this.getTarget();
            return target != null && DemonEntity.this.isTarget(target, TargetPredicate.DEFAULT);
        }
    }

    /**
     * Run Away Goal: Custom behavior to run away when runAway is true
     */
    static class RunAwayGoal extends Goal {
        private final DemonEntity demon;
        private double targetX;
        private double targetY;
        private double targetZ;
        private int cooldown;

        public RunAwayGoal(DemonEntity demon) {
            this.demon = demon;
            this.setControls(EnumSet.of(Control.MOVE));
            this.cooldown = 0;
        }

        @Override
        public boolean canStart() {
            return this.demon.runAway && this.cooldown <= 0;
        }

        @Override
        public boolean shouldContinue() {
            return this.demon.runAway && this.demon.getNavigation().isFollowingPath();
        }

        @Override
        public void start() {
            net.minecraft.util.math.random.Random random = this.demon.getRandom();
            Vec3d currentPos = this.demon.getPos();

            this.targetX = currentPos.x + (random.nextDouble() * 800 - 400);  // Increased flee distance
            this.targetY = currentPos.y + (random.nextDouble() * 200 - 100);  // Adjusted altitude range
            this.targetZ = currentPos.z + (random.nextDouble() * 800 - 400);

            this.targetY = Math.max(50, Math.min(this.targetY, this.demon.getEntityWorld().getHeight() - 50));  // Ensure high altitude

            this.demon.targetPosition = new Vec3d(this.targetX, this.targetY, this.targetZ);
            this.demon.movementType = DemonMovementType.RUNAWAY;

            this.cooldown = 200;  // Increased cooldown
        }

        @Override
        public void tick() {
            if (this.cooldown > 0) {
                this.cooldown--;
            }

            if (this.demon.getPos().distanceTo(new Vec3d(this.targetX, this.targetY, this.targetZ)) < 2.0) {
                this.demon.movementType = DemonMovementType.PATROL;
            }

            if (this.demon.getY() < -50 || this.demon.getY() > this.demon.getEntityWorld().getHeight()) {
                this.demon.remove(RemovalReason.DISCARDED);
            }
        }
    }

    /**
     * Possess Villager Goal: Possess nearby villagers
     */
    class PossessVillagerGoal extends Goal {
        private final DemonEntity demon;

        public PossessVillagerGoal(DemonEntity demon) {
            this.demon = demon;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (this.demon.runAway) {
                return false;
            }
            List<VillagerEntity> nearbyVillagers = this.demon.getEntityWorld()
                    .getEntitiesByClass(VillagerEntity.class, this.demon.getBoundingBox().expand(5.0), LivingEntity::isAlive);
            // Safely remove DemonVillagers
            nearbyVillagers.removeIf(villager -> villager instanceof DemonVillager);
            return !nearbyVillagers.isEmpty();
        }

        @Override
        public void start() {
            List<VillagerEntity> nearbyVillagers = this.demon.getEntityWorld()
                    .getEntitiesByClass(VillagerEntity.class, this.demon.getBoundingBox().expand(5.0), LivingEntity::isAlive);
            // Safely remove DemonVillagers
            nearbyVillagers.removeIf(villager -> villager instanceof DemonVillager);
            if (!nearbyVillagers.isEmpty()) {
                VillagerEntity targetVillager = nearbyVillagers.get(0);

                // Replace the villager with a DemonVillager
                World world = targetVillager.getEntityWorld();
                DemonVillager demonVillager = new DemonVillager(ModEntities.DEMON_VILLAGER, world);
                demonVillager.possessVillager(targetVillager);  // Implement this method as needed

                demonVillager.refreshPositionAndAngles(
                        targetVillager.getX(),
                        targetVillager.getY(),
                        targetVillager.getZ(),
                        targetVillager.getYaw(),
                        targetVillager.getPitch()
                );

                this.demon.remove(RemovalReason.DISCARDED);
                world.playSound(null, demonVillager.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL,
                        SoundCategory.HOSTILE, 1.0F, 1.0F);
                world.spawnEntity(demonVillager);
            }
        }
    }

    /**
     * Seek Villager Goal: Seek and target nearby villagers
     */
    class SeekVillagerGoal extends ActiveTargetGoal<VillagerEntity> {
        private final DemonEntity demon;

        public SeekVillagerGoal(DemonEntity demon) {
            super(demon, VillagerEntity.class, true);
            this.demon = demon;
        }

        @Override
        public boolean canStart() {
            if (this.demon.runAway) {
                return false;
            }
            List<VillagerEntity> nearbyVillagers = this.demon.getEntityWorld()
                    .getEntitiesByClass(VillagerEntity.class, this.demon.getBoundingBox().expand(500.0), LivingEntity::isAlive);  // Expanded range
            // Safely remove DemonVillagers
            nearbyVillagers.removeIf(villager -> villager instanceof DemonVillager);
            if (!nearbyVillagers.isEmpty()) {
                VillagerEntity targetVillager = nearbyVillagers.get(0);
                this.demon.targetPosition = new Vec3d(targetVillager.getX(), targetVillager.getY(), targetVillager.getZ());
                this.demon.movementType = DemonMovementType.SWOOP;
                return true;
            }
            return false;
        }

        @Override
        public void stop() {
            this.demon.getNavigation().stop();
        }
    }
}

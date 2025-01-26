package net.jacobwasbeast.supernatural.entities;

import net.jacobwasbeast.supernatural.api.PsalmTargetManager;
import net.jacobwasbeast.supernatural.api.RitualManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;

import java.util.*;

public class DemonVillager extends VillagerEntity {

    private NbtCompound originalVillagerData;

    public DemonVillager(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer createDemonVillagerAttributes() {
        return VillagerEntity.createVillagerAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5).build();
    }

    @Override
    protected void initGoals() {
        // DO random Villager stuff
        super.initGoals();
        // Target Iron Golems to attack them
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));

        // Remove torches
        this.goalSelector.add(2, new RemoveTorchesGoal(this));

        // Kill Iron Golems
        this.goalSelector.add(3, new Haunt(this));

        // Target players
        this.targetSelector.add(2, new SpecificPlayerTargetGoal(this));
    }


    @Override
    public void onDamaged(DamageSource damageSource) {
        if (damageSource.getAttacker() != null) {
            if (damageSource.getAttacker() instanceof ZombieEntity) {
                damageSource.getAttacker().damage(getDamageSources().magic(), 10.0F);
            }
        }
        super.onDamaged(damageSource);
    }

    @Override
    public boolean canSummonGolem(long time) {
        return false;
    }

    @Override
    public void trade(TradeOffer offer) {
        // Do nothing
    }
    /**
     * Copy the appearance and profession of a nearby villager.
     */
    public void possessVillager(VillagerEntity villager) {
        // Save the original villager's NBT data
        this.originalVillagerData = new NbtCompound();
        villager.writeNbt(this.originalVillagerData);

        // Copy the appearance and profession from the villager
        this.setVillagerData(villager.getVillagerData());
        this.setCustomName(villager.getCustomName());
        this.setPosition(villager.getPos());  // Set the demon-villager's position to the villager's position

        // Remove the original villager from the world
        villager.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        this.ejectVillager();
    }

    public void ejectVillager() {
        if (this.originalVillagerData==null) {
            originalVillagerData = new NbtCompound();
            VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, this.getEntityWorld());
            villager.setVillagerData(this.getVillagerData());
            villager.setCustomName(this.getCustomName());
            villager.setPosition(this.getPos());
            villager.writeNbt(originalVillagerData);
            originalVillagerData = villager.writeNbt(originalVillagerData);
            villager.remove(RemovalReason.DISCARDED);
        }
        if (this.originalVillagerData != null && !this.getEntityWorld().isClient) {
            // Create a new villager entity
            VillagerEntity restoredVillager = EntityType.VILLAGER.create(this.getEntityWorld());

            if (restoredVillager != null) {
                // Restore the villager's data
                restoredVillager.readNbt(this.originalVillagerData);

                // Set the restored villager's position to the current position of the demon-villager
                Vec3d position = this.getPos();
                restoredVillager.refreshPositionAndAngles(position.x, position.y, position.z, this.getYaw(), this.getPitch());

                // Spawn the restored villager in the world
                if (this.getHealth()<50) {
                    restoredVillager.setHealth(0);
                }
                this.getEntityWorld().spawnEntity(restoredVillager);

                // Remove the demon-villager from the world after ejection
                this.remove(RemovalReason.DISCARDED);
                DemonEntity demon = new DemonEntity(net.jacobwasbeast.supernatural.ModEntities.DEMON, this.getEntityWorld());
                demon.refreshPositionAndAngles(position.x, position.y, position.z, this.getYaw(), this.getPitch());
                demon.runAway = true;
                this.getEntityWorld().spawnEntity(demon);
            }
        }
    }

    /**
     * Save the demon villager's copied data to NBT.
     */
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.originalVillagerData != null) {
            nbt.put("OriginalVillager", this.originalVillagerData);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("OriginalVillager")) {
            this.originalVillagerData = nbt.getCompound("OriginalVillager");
        }
    }

    /**
     * Custom AI Goal for DemonVillagers to target specific players.
     */
    static class SpecificPlayerTargetGoal extends ActiveTargetGoal<PlayerEntity> {
        private final DemonVillager demonVillager;

        public SpecificPlayerTargetGoal(DemonVillager demonVillager) {
            super(demonVillager, PlayerEntity.class, true);
            this.demonVillager = demonVillager;
            this.setControls(EnumSet.of(Control.TARGET));
        }

        @Override
        public boolean canStart() {
            // Check if there are any players in the PsalmTargetManager
            Set<UUID> targetedPlayerUUIDs = PsalmTargetManager.getInstance().getTargetedPlayers();
            if (targetedPlayerUUIDs.isEmpty()) {
                return false;
            }
            return true;
        }

        @Override
        public boolean shouldContinue() {
            PlayerEntity target = this.getTarget();
            if (target == null || !PsalmTargetManager.getInstance().isTargeted(target)) {
                return false;
            }
            return true;
        }

        @Override
        public void tick() {
            super.tick();
            PlayerEntity target = this.getTarget();
            if (target != null) {
                this.demonVillager.navigation.startMovingTo(target, 1.0);
                if (this.demonVillager.squaredDistanceTo(target) < 2.0) {
                    this.demonVillager.attackLivingEntity(target);
                    target.damage(this.demonVillager.getDamageSources().mobAttack(this.demonVillager), 2.0F);
                }
            }
        }

        public PlayerEntity getTarget() {
            World world = this.demonVillager.getEntityWorld();
            Set<UUID> targetedPlayerUUIDs = PsalmTargetManager.getInstance().getTargetedPlayers();
            for (UUID playerUUID : targetedPlayerUUIDs) {
                Entity entity = world.getPlayerByUuid(playerUUID);
                if (entity instanceof PlayerEntity) {
                    if (this.demonVillager.getBlockPos().isWithinDistance(entity.getBlockPos(), 50.0)) {
                        return (PlayerEntity) entity;
                    }
                }
            }
            return null;
        }
    }

    // Custom AI goal to kill iron golems
    static class Haunt extends Goal {
        private final DemonVillager demonVillager;
        private int cooldown = 0;
        public Haunt(DemonVillager demonVillager) {
            this.demonVillager = demonVillager;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            // Check if the demon is near living iron golems (within a 10-block radius)
            if (this.cooldown > 0) {
                this.cooldown--;
            }
            else {
                BlockPos demonPos = this.demonVillager.getBlockPos();
                boolean hasGolem = this.demonVillager.getEntityWorld().getEntitiesByClass(IronGolemEntity.class, demonVillager.getBoundingBox().expand(100.0), LivingEntity::isAlive).size() > 0;
                if (hasGolem) {
                    for (IronGolemEntity golem : this.demonVillager.getEntityWorld().getEntitiesByClass(IronGolemEntity.class, demonVillager.getBoundingBox().expand(30.0), LivingEntity::isAlive)) {
                        demonVillager.navigation.startMovingTo(golem, 1.0);
                        if (demonVillager.squaredDistanceTo(golem) < 10.0) {
                            demonVillager.attackLivingEntity(golem);
                            golem.damage(demonVillager.getDamageSources().magic(), 10.0F);
                            cooldown = 100;
                        }
                    }
                }
                else {
                    boolean hasVillagers = this.demonVillager.getEntityWorld().getEntitiesByClass(VillagerEntity.class, demonVillager.getBoundingBox().expand(10.0), LivingEntity::isAlive).size() > 0;
                    Random random = new Random();
                    if (random.nextInt(100) < 5 && hasVillagers) {
                        List<VillagerEntity> villagers = this.demonVillager.getEntityWorld().getEntitiesByClass(VillagerEntity.class, demonVillager.getBoundingBox().expand(10.0), LivingEntity::isAlive);
                        demonVillager.navigation.startMovingTo(villagers.get(random.nextInt(villagers.size())), 1.0);
                    }
                    if (!hasVillagers) {
                        Random random2 = new Random();
                        if (random2.nextInt(1000) < 5) {
                            demonVillager.ejectVillager();
                        }
                    }
                }
            }
            return false;
        }
    }

    static class RemoveTorchesGoal extends Goal {
        private final DemonVillager demonVillager;

        public RemoveTorchesGoal(DemonVillager demonVillager) {
            this.demonVillager = demonVillager;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            // Check if the demon is near any torches (within a 10-block radius)
            BlockPos demonPos = this.demonVillager.getBlockPos();
            for (BlockPos pos : BlockPos.iterate(demonPos.add(-10, -5, -10), demonPos.add(10, 5, 10))) {
                if (this.demonVillager.getEntityWorld().getBlockState(pos).getBlock() == Blocks.TORCH) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            // Find and remove the nearest torch
            BlockPos demonPos = this.demonVillager.getBlockPos();
            for (BlockPos pos : BlockPos.iterate(demonPos.add(-10, -5, -10), demonPos.add(10, 5, 10))) {
                if (this.demonVillager.getEntityWorld().getBlockState(pos).getBlock() == Blocks.TORCH) {
                    this.demonVillager.getEntityWorld().breakBlock(pos, true);  // Remove the torch
                    this.demonVillager.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);  // Play sound for removing torch
                    break;
                }
            }
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.getStackInHand(hand).getItem() == Items.NAME_TAG) {
            return super.interactMob(player, hand);
        }
        return ActionResult.FAIL;
    }

    @Override
    public void tick() {
        super.tick();
        if (RitualManager.getInstance().isInDevilsTrap(this.getWorld(), this.getBlockPos())) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,10,255));
        }
    }
}

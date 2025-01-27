package net.jacobwasbeast.supernatural.items;

import net.jacobwasbeast.supernatural.ModEntities;
import net.jacobwasbeast.supernatural.ModItems;
import net.jacobwasbeast.supernatural.entities.ColtBulletEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ColtItem extends Item {
    public ColtItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;
            int timeUsed = this.getMaxUseTime(stack) - remainingUseTicks;

            // Only reload if the player is sneaking
            if (player.isSneaking()) {
                loadRound(stack, world, player);  // Reload when sneaking
            } else {
                if (timeUsed >= 10) {  // Only shoot when player is not sneaking and gun is ready
                    NbtCompound tag = stack.getOrCreateNbt();
                    if (tag.contains("ammo")) {
                        int currentAmmo = tag.getInt("ammo");
                        if (currentAmmo > 0) {
                            // If the gun has ammo, shoot it
                            shoot(stack, world, player);
                            tag.putInt("ammo", currentAmmo - 1); // Decrease ammo by 1
                        } else {
                            player.sendMessage(Text.of("No ammo! Reload by sneaking."), true);
                        }
                    } else {
                        tag.putInt("ammo", 0); // Initialize ammo if missing
                    }
                }
            }
        }
    }

    private void shoot(ItemStack stack, World world, PlayerEntity player) {
        if (!world.isClient) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1.0F, 1.0F);

            ColtBulletEntity bullet = new ColtBulletEntity(ModEntities.COLT_BULLET, player, world);
            Vec3d look = player.getRotationVec(1.0F);

            bullet.setVelocity(
                    look.x,
                    look.y,
                    look.z,
                    150F,
                    0.0F
            );

            world.spawnEntity(bullet);
        }
    }

    public void loadRound(ItemStack stack, World world, PlayerEntity player) {
        int currentAmmo = 0;
        NbtCompound tag = stack.getOrCreateNbt();
        if (tag.contains("ammo")) {
            currentAmmo = tag.getInt("ammo");
        }
        if (currentAmmo < 6) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack invStack = player.getInventory().getStack(i);
                if (invStack.getItem() == ModItems.COLT_BULLET) {
                    invStack.decrement(1);
                    tag.putInt("ammo", currentAmmo + 1);
                    player.sendMessage(Text.of("Reloaded 1 bullet into the Colt. Ammo: " + (currentAmmo + 1) + "/6"), true);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return;  // Stop after finding and loading one bullet
                }
            }
            player.sendMessage(Text.of("No bullets left in your inventory!"), true);
        } else {
            player.sendMessage(Text.of("The Colt is fully loaded!"), true);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;
            int timeUsed = this.getMaxUseTime(stack) - remainingUseTicks;
            NbtCompound tag = stack.getOrCreateNbt();

            // Play a ready sound when the Colt is charged enough
            if (timeUsed == 10 && tag.getInt("ammo") > 0 && !player.isSneaking()) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }

            // Only reload bullets if holding down past the ready point and sneaking
            if (timeUsed > 10 && tag.getInt("ammo") < 6 && player.isSneaking()) {
                loadRound(stack, world, player);
            }
        }
    }
}

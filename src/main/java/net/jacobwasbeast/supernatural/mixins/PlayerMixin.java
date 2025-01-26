package net.jacobwasbeast.supernatural.mixins;

import net.jacobwasbeast.supernatural.ModItems;
import net.jacobwasbeast.supernatural.api.PsalmTargetManager;
import net.jacobwasbeast.supernatural.items.Psalm;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerMixin {
    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getInventory().contains(ModItems.PSALM.getDefaultStack())) {
            Psalm psalm = (Psalm) player.getInventory().getStack(player.getInventory().getSlotWithStack(ModItems.PSALM.getDefaultStack())).getItem();
            psalm.resetPsalm();
            PsalmTargetManager.getInstance().removeTarget(player);
        }
    }

}

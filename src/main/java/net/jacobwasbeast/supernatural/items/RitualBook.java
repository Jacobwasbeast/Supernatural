package net.jacobwasbeast.supernatural.items;

import net.jacobwasbeast.supernatural.client.gui.RitualGUI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public class RitualBook extends Item {

    public RitualBook(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient) {
            net.minecraft.client.MinecraftClient.getInstance().setScreen(new RitualGUI());
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }
}

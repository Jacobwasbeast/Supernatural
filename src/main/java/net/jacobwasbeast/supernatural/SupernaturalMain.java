package net.jacobwasbeast.supernatural;

import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.jacobwasbeast.supernatural.api.PsalmTargetManager;
import net.minecraft.util.Identifier;
public class SupernaturalMain implements ModInitializer {

    @Override
    public void onInitialize() {
        FieldRegistrationHandler.register(ModEntities.class, "supernatural", false);
        FieldRegistrationHandler.register(ModItems.class, "supernatural", false);
        FieldRegistrationHandler.register(ModBlocks.class, "supernatural", false);
        FieldRegistrationHandler.register(ModBlockEntities.class, "supernatural", false);
        PsalmTargetManager.getInstance();
        ServerPlayConnectionEvents.DISCONNECT.register((player, server) -> {
            PsalmTargetManager.getInstance().removeTarget(player.getPlayer());
        });
        ModMisc.registerMisc();
    }

    public static Identifier id(String path) {
        return new Identifier("supernatural", path.toLowerCase());
    }
}

package net.jacobwasbeast.supernatural.client;

import net.fabricmc.api.ClientModInitializer;

public class SupernaturalMainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModEntityRenderer.registerEntityRenderers();
    }
}

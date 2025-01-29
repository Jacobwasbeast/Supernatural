package net.jacobwasbeast.supernatural.client.renderer.blocks;

import net.jacobwasbeast.supernatural.blocks.entities.HauntedBonesEntity;
import net.jacobwasbeast.supernatural.client.models.blocks.HauntedBonesModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class HauntedBonesRenderer extends GeoBlockRenderer<HauntedBonesEntity> {
    public HauntedBonesRenderer() {
        super(new HauntedBonesModel());
    }
}

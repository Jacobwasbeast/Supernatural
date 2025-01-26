package net.jacobwasbeast.supernatural.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.jacobwasbeast.supernatural.api.RitualManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RitualGUI extends BaseOwoScreen<FlowLayout> {
    ArrayList<Component> components = new ArrayList<>();
    int page = 1;
    int maxPage = 0;
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        RitualManager.getInstance().rituals.forEach((id, ritual) -> {
            Identifier reference = ritual.reference;
            reference = reference.withPath("textures/gui/" + reference.getPath() + ".png");
            components.add(Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .child(Components.button(Text.of(ritual.name), button -> {
                        System.out.println("click");
                    }))
                    .padding(Insets.of(10))
                    .surface(Surface.DARK_PANEL)
                    .verticalAlignment(VerticalAlignment.CENTER)
                    .horizontalAlignment(HorizontalAlignment.CENTER));
            components.add(Components.texture(reference, 0, 0, 100, 100, 100, 100));
            components.add(Components.label(Text.of(ritual.description)));
        });
        maxPage = components.size() / 3;
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }
    boolean isInitialized = false;

    @Override
    protected void build(FlowLayout rootComponent) {
        if (!isInitialized) {
            isInitialized = true;
            rootComponent
                    .surface(Surface.VANILLA_TRANSLUCENT)
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .verticalAlignment(VerticalAlignment.CENTER);
        }
        // add current page and max page
        Component pageComp = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.of("Page " + page + "/" + maxPage)))
                .padding(Insets.of(10))
                .surface(Surface.DARK_PANEL)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);
        // Add two components to the root component the ritual and reference
        int start = (page - 1) * 3;
        int end = Math.min(page * 3, components.size());
        ArrayList<Component> componentsCanvas = new ArrayList<>();
        Component prev = Components.button(Text.of("Previous"), button -> {
            if (page > 1) {
                page--;
                for (Component component : componentsCanvas) {
                    component.remove();
                }
                this.build(rootComponent);
            }
        });
        Component next = Components.button(Text.of("Next"), button -> {
            if (page < maxPage) {
                page++;
                for (Component component : componentsCanvas) {
                    component.remove();
                }
                this.build(rootComponent);
            }
        });
        componentsCanvas.add(prev);
        componentsCanvas.add(next);
        componentsCanvas.add(pageComp);
        Component combined = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(prev)
                .child(next)
                .padding(Insets.of(10))
                .surface(Surface.DARK_PANEL)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);
        componentsCanvas.add(combined);
        for (int i = start; i < end; i++) {
            componentsCanvas.add(components.get(i));
            rootComponent.child(components.get(i));
        }
        rootComponent.child(combined);
    }
}

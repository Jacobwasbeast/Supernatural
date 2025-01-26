package net.jacobwasbeast.supernatural.api;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Singleton manager to track players who have read the Psalm.
 */
public class PsalmTargetManager {
    private static final PsalmTargetManager INSTANCE = new PsalmTargetManager();

    // Set of player UUIDs who have read the Psalm
    private final Set<UUID> targetedPlayers = Collections.synchronizedSet(new HashSet<>());

    // Private constructor to enforce singleton pattern
    private PsalmTargetManager() {}

    // Get the singleton instance
    public static PsalmTargetManager getInstance() {
        return INSTANCE;
    }

    /**
     * Adds a player to the targeted list.
     *
     * @param player The player who read the Psalm.
     */
    public void addTarget(PlayerEntity player) {
        player.sendMessage(Text.of("You will now be targeted by demons!"), true);
        targetedPlayers.add(player.getUuid());
    }

    /**
     * Removes a player from the targeted list.
     *
     * @param player The player to remove.
     */
    public void removeTarget(PlayerEntity player) {
        player.sendMessage(Text.of("You are no longer targeted by demons."), true);
        targetedPlayers.remove(player.getUuid());
    }

    /**
     * Checks if a player is in the targeted list.
     *
     * @param player The player to check.
     * @return True if the player is targeted, false otherwise.
     */
    public boolean isTargeted(PlayerEntity player) {
        return targetedPlayers.contains(player.getUuid());
    }

    /**
     * Retrieves all targeted player UUIDs.
     *
     * @return An unmodifiable set of targeted player UUIDs.
     */
    public Set<UUID> getTargetedPlayers() {
        return Collections.unmodifiableSet(targetedPlayers);
    }

    /**
     * Clears all targeted players. Useful for resetting or during server shutdown.
     */
    public void clearTargets() {
        targetedPlayers.clear();
    }
}

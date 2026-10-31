/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClickPlayerListener implements Listener {

    private final PowerMining plugin;
    private final boolean useDurabilityPerBlock;
    private final DebuggingMessages debuggingMessages;

    public ClickPlayerListener(@NotNull final PowerMining plugin) {
        this.plugin = plugin;
        this.debuggingMessages = plugin.getDebuggingMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    /**
     * Handles right-click interactions when players use custom power hoes (Plows)
     * or path-makers to till or flatten multiple blocks in an area at once.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerUse(@NotNull final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack handItem = player.getInventory().getItemInMainHand();
        final Material handItemType = handItem.getType();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        // Run sanity checks to see if we should ignore this interaction
        if (shouldCancelAction(action, player, handItem, handItemType, block)) {
            return;
        }

        // Look up the block face they clicked using their unique UUID
        final PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
        final BlockFace blockFace = pil.getBlockFaceByPlayer(player.getUniqueId());

        // Loop through surrounding blocks to perform area tilling or path creation
        for (Block target : PowerUtils.getSurroundingBlocksFarm(blockFace, block, Reference.RADIUS)) {
            final Material targetType = target.getType();

            // Skip blocks the player isn't allowed to modify (e.g., WorldGuard regions)
            if (!PowerUtils.canBreak(plugin, player, target)) {
                continue;
            }

            // PowerPlow logic: converts dirt/grass blocks into farmland
            if (PowerUtils.validatePlow(handItemType, targetType)) {
                debuggingMessages.sendConsoleMessage(ChatColor.RED + "Tilling: " + targetType);
                usePowerTool(player, handItem, target, Material.FARMLAND);
                continue;
            }

            // PowerPath logic: converts dirt/grass blocks into dirt paths
            if (PowerUtils.validatePath(handItemType, targetType)) {
                usePowerTool(player, handItem, target, Material.DIRT_PATH);
            }
        }

        // If durability is set to drop once per use instead of per-block, apply it here
        if (!useDurabilityPerBlock && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Replaces the target block with the new material and handles per-block durability wear if enabled.
     */
    private void usePowerTool(
            @NotNull final Player player,
            @NotNull final ItemStack handItem,
            @NotNull final Block block,
            @NotNull final Material newMaterial
    ) {
        block.setType(newMaterial);
        if (useDurabilityPerBlock && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Filters out unwanted actions (like left clicks, air clicks, sneaking, or empty hands).
     * Returns true if the interaction event should be completely ignored.
     */
    private boolean shouldCancelAction(
            @NotNull final Action action,
            @NotNull final Player player,
            @NotNull final ItemStack handItem,
            @NotNull final Material handItemType,
            @Nullable final Block block
    ) {
        // We only care about right-clicking physical blocks
        if (action == Action.LEFT_CLICK_BLOCK
                || action == Action.LEFT_CLICK_AIR
                || action == Action.RIGHT_CLICK_AIR) {
            return true;
        }

        // Sneaking bypasses area tilling
        if (player.isSneaking()) {
            return true;
        }

        if (handItemType == Material.AIR) {
            return true;
        }

        if (block == null) {
            return true;
        }

        // Ensure the block clicked is actually something we can till/path
        if (!PowerUtils.isTillable(block.getType())) {
            return true;
        }

        // Ensure they are holding one of our custom power tools
        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }

        // Finally, make sure they have permission to use this tool type
        return !PowerUtils.checkUsePermission(player, handItemType);
    }
}
/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class handles PowerTool right-click actions — specifically for Plows and Path makers.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClickPlayerListener implements Listener {

    private final PowerMining plugin;
    private final boolean useDurabilityPerBlock;
    private final DebuggingMessages debuggingMessages;

    public ClickPlayerListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        this.debuggingMessages = plugin.getDebuggingMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerUse(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack handItem = player.getInventory().getItemInMainHand();
        final Material handItemType = handItem.getType();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        // Exit early if checks fail
        if (shouldCancelAction(action, player, handItem, handItemType, block)) {
            return;
        }

        final String playerName = player.getName();
        final PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
        final BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

        // Perform 3x3 tilling or path-making
        for (Block target : PowerUtils.getSurroundingBlocksFarm(blockFace, block, Reference.RADIUS)) {
            final Material targetType = target.getType();

            // Skip if player cannot modify the block
            if (!PowerUtils.canBreak(plugin, player, target)) {
                continue;
            }

            // Handle PowerPlow — turns dirt/grass into farmland
            if (PowerUtils.validatePlow(handItemType, targetType)) {
                debuggingMessages.sendConsoleMessage(ChatColor.RED + "Tilling: " + targetType);
                usePowerTool(player, handItem, target, Material.FARMLAND);
                continue;
            }

            // Handle PowerPath — turns grass/dirt into dirt path
            if (PowerUtils.validatePath(handItemType, targetType)) {
                usePowerTool(player, handItem, target, Material.DIRT_PATH);
            }
        }

        // Reduce tool durability once if not using per-block mode
        if (!useDurabilityPerBlock && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Replaces the target block and optionally reduces tool durability.
     */
    private void usePowerTool(
            @Nonnull final Player player,
            @Nonnull final ItemStack handItem,
            @Nonnull final Block block,
            @Nonnull final Material newMaterial
    ) {
        block.setType(newMaterial);
        if (useDurabilityPerBlock && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Performs all preconditions before activating PowerTool behavior.
     *
     * @return true if the event should be ignored
     */
    private boolean shouldCancelAction(
            @Nonnull final Action action,
            @Nonnull final Player player,
            @Nonnull final ItemStack handItem,
            @Nonnull final Material handItemType,
            @Nullable final Block block
    ) {
        // Ignore clicks that don’t target a block
        if (action == Action.LEFT_CLICK_BLOCK
                || action == Action.LEFT_CLICK_AIR
                || action == Action.RIGHT_CLICK_AIR) {
            return true;
        }

        if (player.isSneaking()) {
            return true;
        }

        if (handItemType == Material.AIR) {
            return true;
        }

        if (block == null) {
            return true;
        }

        if (!PowerUtils.isTillable(block.getType())) {
            return true;
        }

        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }

        return !PowerUtils.checkUsePermission(player, handItemType);
    }
}

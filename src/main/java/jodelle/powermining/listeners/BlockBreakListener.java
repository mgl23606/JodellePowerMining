/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for handling the actual mining when using a Hammer or Excavator
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class BlockBreakListener implements Listener {

    private final PowerMining plugin;
    private final boolean useDurabilityPerBlock;
    private final DebuggingMessages debuggingMessages;

    private Player player;
    private ItemStack handItem;

    public BlockBreakListener(@Nonnull PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.debuggingMessages = plugin.getDebuggingMessages();

        // JSON-based config still exposes getConfig() wrapper
        this.useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        this.player = event.getPlayer();
        this.handItem = player.getInventory().getItemInMainHand();

        boolean debugging = false;
        debuggingMessages.sendConsoleMessage(debugging, ChatColor.RED + "Broke a block: " + event.getBlock().getType());

        // Stop if the tool or situation fails checks
        if (shouldCancelAction()) {
            return;
        }

        final Block centerBlock = event.getBlock();
        final String playerName = player.getName();

        final PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
        final BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

        // Breaks surrounding blocks that match the corresponding tool
        for (Block block : PowerUtils.getSurroundingBlocks(blockFace, centerBlock, Reference.RADIUS, Reference.DEEP)) {
            checkAndBreakBlock(block);
        }

        // Reduce durability once if per-block damage is disabled
        if (!useDurabilityPerBlock && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Attempts to break a block if the player’s held PowerTool is valid for it.
     *
     * @param block The block being broken by the player
     */
    private void checkAndBreakBlock(@Nonnull Block block) {
        Material blockMat = block.getType();

        boolean useHammer = PowerUtils.validateHammer(handItem.getType(), blockMat);
        boolean useExcavator = !useHammer && PowerUtils.validateExcavator(handItem.getType(), blockMat);

        if (!useHammer && !useExcavator) {
            return;
        }

        // Check if player has permission to break this block (WorldGuard, region plugins, etc.)
        if (!PowerUtils.canBreak(plugin, player, block)) {
            return;
        }

        // Break block and handle tool durability
        if (block.breakNaturally(handItem) && player.getGameMode() == GameMode.SURVIVAL) {
            if (useDurabilityPerBlock) {
                PowerUtils.reduceDurability(player, handItem);
            }
        }
    }

    /**
     * Performs the basic verifications before executing PowerTool logic.
     *
     * @return true if the event should be cancelled (not a valid PowerTool action)
     */
    private boolean shouldCancelAction() {
        // If the player is sneaking, act like a normal pickaxe/shovel
        if (player.isSneaking()) {
            return true;
        }

        Material handItemType = handItem.getType();
        if (handItemType == Material.AIR) {
            return true;
        }

        // Not a PowerTool? Do nothing special.
        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }

        // Player lacks permission to use this PowerTool
        return !PowerUtils.checkUsePermission(player, handItemType);
    }
}

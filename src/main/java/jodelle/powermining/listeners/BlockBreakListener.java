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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockBreakListener implements Listener {

    private final PowerMining plugin;
    private final boolean useDurabilityPerBlock;
    private final DebuggingMessages debuggingMessages;

    public BlockBreakListener(@NotNull PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.debuggingMessages = plugin.getDebuggingMessages();

        this.useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    /**
     * Listens for block breaks to trigger area mining when a player uses a hammer or excavator.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ItemStack handItem = player.getInventory().getItemInMainHand();

        boolean debugging = false;
        debuggingMessages.sendConsoleMessage(debugging, ChatColor.RED + "Broke a block: " + event.getBlock().getType());

        // Check if we should ignore this event (e.g. player is sneaking, item isn't a power tool)
        if (shouldCancelAction(player, handItem)) {
            return;
        }

        final Block centerBlock = event.getBlock();
        final PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
        final BlockFace blockFace = pil.getBlockFaceByPlayer(player.getUniqueId());

        // Loop through surrounding blocks based on the radius and depth config settings
        for (Block block : PowerUtils.getSurroundingBlocks(blockFace, centerBlock, Reference.RADIUS, Reference.DEEP)) {
            checkAndBreakBlock(player, handItem, block);
        }

        // If durability is configured to drop once per use rather than per block, apply it here
        if (!useDurabilityPerBlock && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Validates whether a surrounding block matches the tool type, checks protections, and breaks it.
     */
    private void checkAndBreakBlock(@NotNull Player player, @NotNull ItemStack handItem, @NotNull Block block) {
        Material blockMat = block.getType();

        boolean useHammer = PowerUtils.validateHammer(handItem.getType(), blockMat);
        boolean useExcavator = !useHammer && PowerUtils.validateExcavator(handItem.getType(), blockMat);

        if (!useHammer && !useExcavator) {
            return;
        }

        // Ensure the player is allowed to break this block (respects WorldGuard and land claims)
        if (!PowerUtils.canBreak(plugin, player, block)) {
            return;
        }

        // Break the block naturally and take durability wear per block if enabled
        if (block.breakNaturally(handItem) && player.getGameMode() == GameMode.SURVIVAL) {
            if (useDurabilityPerBlock) {
                PowerUtils.reduceDurability(player, handItem);
            }
        }
    }

    /**
     * Checks initial pre-conditions. Returns true if the event should be ignored.
     */
    private boolean shouldCancelAction(@NotNull Player player, @NotNull ItemStack handItem) {
        // Sneaking lets players mine single blocks normally without triggering area effects
        if (player.isSneaking()) {
            return true;
        }

        Material handItemType = handItem.getType();
        if (handItemType == Material.AIR) {
            return true;
        }

        // Ignore standard vanilla items
        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }

        // Verify the player has permission to use this specific tool type
        return !PowerUtils.checkUsePermission(player, handItemType);
    }
}
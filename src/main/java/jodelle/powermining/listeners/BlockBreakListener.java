/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for handling the actual mining when using a Hammer/Excavator
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import javax.annotation.Nonnull;

public class BlockBreakListener implements Listener {
    public final PowerMining plugin;
    public final boolean useDurabilityPerBlock;
    public final DebuggingMessages debuggingMessages;

    private Player player;
    private ItemStack handItem;

    public BlockBreakListener(@Nonnull PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        debuggingMessages = plugin.getDebuggingMessages();
        useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        player = event.getPlayer();
        handItem = player.getInventory().getItemInMainHand();

        if (handItem != null && handItem.getItemMeta() != null && handItem.getItemMeta().hasDisplayName()) {
            debuggingMessages.sendConsoleMessage(
                    ChatColor.RED + "Broke a block with item: " + handItem.getItemMeta().getDisplayName());
        }

        if (basicVerifications()) {
            return;
        }

        final Block centerBlock = event.getBlock();
        final String playerName = player.getName();

        final PlayerInteractListener pil = plugin.getPlayerInteractHandler() != null
                ? plugin.getPlayerInteractHandler().getListener()
                : null;

        if (pil == null) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "PlayerInteractListener is null.");
            return;
        }

        final BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

        int radius = Math.max(0, plugin.getConfig().getInt("Radius", Reference.RADIUS) - 1);
        int depth = Math.max(0, plugin.getConfig().getInt("Depth", Reference.DEPTH) - 1);

        debuggingMessages.sendConsoleMessage("(processConfig) Value for radius is: (Reference) " + Reference.RADIUS
                + " (Config) " + (plugin.getConfig().getInt("Radius", 2) - 1));
        debuggingMessages.sendConsoleMessage("(processConfig) Value for depth is: (Reference) " + Reference.DEPTH
                + " (Config) " + (plugin.getConfig().getInt("Depth", 1) - 1));

        List<Block> surroundingBlocks = PowerUtils.getSurroundingBlocks(blockFace, centerBlock, radius, depth);

        if (surroundingBlocks.isEmpty()) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "No surrounding blocks found.");
            return;
        }

        int totalExp = event.getExpToDrop();

        for (Block block : surroundingBlocks) {
            totalExp += checkAndBreakBlock(block);
        }

        if (!useDurabilityPerBlock && player.getGameMode().equals(GameMode.SURVIVAL)) {
            event.setCancelled(true);
            PowerUtils.reduceDurability(player, handItem);
            centerBlock.breakNaturally(handItem);
        }

        if (totalExp > 0) {
            BlockExpEvent expEvent = new BlockExpEvent(centerBlock, totalExp);
            plugin.getServer().getPluginManager().callEvent(expEvent);

            ExperienceOrb orb = centerBlock.getWorld().spawn(centerBlock.getLocation(), ExperienceOrb.class);
            orb.setExperience(expEvent.getExpToDrop());
        }
    }

	/**
	 * Check and break the block if possible
	 * 
	 * @param block The block being broken by the player
	 */
    private int checkAndBreakBlock(@Nonnull Block block) {
        Material blockMat = block.getType();
        boolean useHammer;
        boolean useExcavator = false;

        if (useHammer = PowerUtils.validateHammer(handItem.getType(), blockMat)) ;
        else if (useExcavator = PowerUtils.validateExcavator(handItem.getType(), blockMat)) ;

        if (useHammer || useExcavator) {
            if (!PowerUtils.canBreak(plugin, player, block)) {
                return 0;
            }

            int expToDrop = block.getDrops().isEmpty() ? 0 : block.getType().getHardness() > 0 ? 1 : 0;

            if (block.breakNaturally(handItem) && player.getGameMode().equals(GameMode.SURVIVAL)) {
                if (useDurabilityPerBlock) {
                    PowerUtils.reduceDurability(player, handItem);
                }
            }

            return expToDrop;
        }

        return 0;
    }

	/**
	 * Perform the basic verifications
	 * 
	 * @return True if the PowerTool is ready to use
	 */
	private boolean basicVerifications() {
		// If the player is sneaking, we want the tool to act like a normal
		// pickaxe/shovel
		if (player.isSneaking()) {
			return true;
		}

		Material handItemType = handItem.getType();

		if (handItemType.equals(Material.AIR)) {
			return true;
		}

		// If this is not a power tool, acts like a normal pickaxe
		if (!PowerUtils.isPowerTool(handItem)) {
			return true;
		}

		// If the player does not have permission to use the tool, acts like a normal
		// pickaxe/shovel

		if (!PowerUtils.checkUsePermission(plugin, player, handItemType)) {
			return true;
		}
		return false;
	}
}

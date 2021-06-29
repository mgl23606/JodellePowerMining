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
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import org.apache.commons.lang.Validate;
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
		boolean debugging = true;

		player = event.getPlayer();
		handItem = player.getInventory().getItemInMainHand();

		debuggingMessages.sendConsoleMessage(debugging, ChatColor.RED + "Broke a block: ");

		if (basicVerifications()){
			return;
		}

		final Block centerBlock = event.getBlock();
		final String playerName = player.getName();

		final PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
		final BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

		// Breaks surrounding blocks as long as they match the corresponding tool
		for (Block block: PowerUtils.getSurroundingBlocks(blockFace, centerBlock, Reference.RADIUS, Reference.DEEP)) {
			checkAndBreakBlock(block);
		}

		if (!useDurabilityPerBlock && player.getGameMode().equals(GameMode.SURVIVAL)){
			PowerUtils.reduceDurability(player, handItem);
		}
	}

	/**
	 * Check and break the block if possible
	 * @param block The block being broken by the player
	 */
	private void checkAndBreakBlock(@Nonnull Block block) {
		Material blockMat = block.getType();

		boolean useHammer;
		boolean useExcavator = false;

		// This bit is necessary to guarantee we only get one or the other as true, otherwise it might break blocks with the wrong tool
		if (useHammer = PowerUtils.validateHammer(handItem.getType(), blockMat));
		else if (useExcavator = PowerUtils.validateExcavator(handItem.getType(), blockMat));

		if (useHammer || useExcavator) {

			// Check if player has permission to break the block
			if (!PowerUtils.canBreak(plugin, player, block)) {
				return;
			}

			//When using breakNaturally the block is broken but the durability of the tool stays the same
			//so it's necessary to update the damage manually
			if(block.breakNaturally(handItem) && player.getGameMode().equals(GameMode.SURVIVAL)){
				if (useDurabilityPerBlock) {
					PowerUtils.reduceDurability(player, handItem);
				}
			}

		}
	}

	/**
	 * Perform the basic verifications
	 * @return True if the PowerTool is ready to use
	 */
	private boolean basicVerifications() {
		// If the player is sneaking, we want the tool to act like a normal pickaxe/shovel
		if (player.isSneaking()) {
			return true;
		}

		Material handItemType = handItem.getType();

		if(handItemType.equals(Material.AIR)) {
			return true;
		}

		// If the player does not have permission to use the tool, acts like a normal pickaxe/shovel

		if (!PowerUtils.checkUsePermission(player, handItemType)) {
			return true;
		}

		// If this is not a power tool, acts like a normal pickaxe
		if (!PowerUtils.isPowerTool(handItem)) {
			return true;
		}
		return false;
	}
}

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

import java.util.Map;


import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.palmergames.bukkit.towny.TownyLogger.log;

public class BlockBreakListener implements Listener {
	public PowerMining plugin;
	public boolean useDurabilityPerBlock;

	public BlockBreakListener(PowerMining plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void checkToolAndBreakBlocks(BlockBreakEvent event) {
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		Player player = event.getPlayer();
		ItemStack handItem = player.getInventory().getItemInMainHand();
		Material handItemType = handItem.getType();

		console.sendMessage(ChatColor.RED + "[JodellePowerMining] Broke a block: ");

		if (player != null && (player instanceof Player)) {
			// If the player is sneaking, we want the tool to act like a normal pickaxe/shovel
			if (player.isSneaking())
				return;

			// If the player does not have permission to use the tool, acts like a normal pickaxe/shovel
			if (!PowerUtils.checkUsePermission(player, handItemType))
				return;

			// If this is not a power tool, acts like a normal pickaxe
			if (!PowerUtils.isPowerTool(handItem))
				return;

			Block block = event.getBlock();
			String playerName = player.getName();

			PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
			BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

			// Breaks surrounding blocks as long as they match the corresponding tool
			for (Block e: PowerUtils.getSurroundingBlocks(blockFace, block, Reference.RADIUS, Reference.DEEP)) {
				Material blockMat = e.getType();
				Location blockLoc = e.getLocation();

				boolean useHammer = false;
				boolean useExcavator = false;
				boolean usePlow = false;

				// This bit is necessary to guarantee we only get one or the other as true, otherwise it might break blocks with the wrong tool
				if (useHammer = PowerUtils.validateHammer(handItem.getType(), blockMat));
				else if (useExcavator = PowerUtils.validateExcavator(handItem.getType(), blockMat));
				else if (usePlow = PowerUtils.validatePlow(handItem.getType(),blockMat));

//				//if (useHammer || useExcavator || usePlow) {
				if (useHammer || useExcavator || usePlow) {

					// Check if player has permission to break the block
					if (!PowerUtils.canBreak(plugin, player, e))
						continue;


					console.sendMessage(ChatColor.RED + "[JodellePowerMining] Breaking: " + e.getType().toString());

					//When using breakNaturally the block is broken but the durability of the tool stays the same
					//so it's necessary to update the damage manually
					if(e.breakNaturally(handItem) && player.getGameMode().equals(GameMode.SURVIVAL)){
						PowerUtils.reduceDurability(handItem);
					}

				}
			}
		}
	}
}

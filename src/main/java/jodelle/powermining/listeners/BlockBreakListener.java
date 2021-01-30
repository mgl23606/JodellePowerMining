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
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
		Player player = event.getPlayer();
		ItemStack handItem = player.getItemInHand();
		Material handItemType = handItem.getType();

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

			Map<Enchantment, Integer> enchants = handItem.getEnchantments();
			Enchantment enchant = null;
			int enchantLevel = 0;
			if (enchants.get(Enchantment.SILK_TOUCH) != null) {
				enchant = Enchantment.SILK_TOUCH;
				enchantLevel = enchants.get(Enchantment.SILK_TOUCH);
			}
			else if (enchants.get(Enchantment.LOOT_BONUS_BLOCKS) != null) {
				enchant = Enchantment.LOOT_BONUS_BLOCKS;
				enchantLevel = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
			}

			short curDur = handItem.getDurability();
			short maxDur = handItem.getType().getMaxDurability();

			// Breaks surrounding blocks as long as they match the corresponding tool
			for (Block e: PowerUtils.getSurroundingBlocks(blockFace, block)) {
				Material blockMat = e.getType();
				Location blockLoc = e.getLocation();

				boolean useHammer = false;
				boolean useExcavator = false;
				//boolean useHoe = false;

				// This bit is necessary to guarantee we only get one or the other as true, otherwise it might break blocks with the wrong tool
				if (useHammer = PowerUtils.validateHammer(handItem.getType(), blockMat));
				else if (useExcavator = PowerUtils.validateExcavator(handItem.getType(), blockMat));
				//else if (useHoe = PowerUtils.validateHoe(handItem.getType(),blockMat));

				//if (useHammer || useExcavator || useHoe) {
				if (useHammer || useExcavator) {

					// Check if player has permission to break the block
					if (!PowerUtils.canBreak(plugin, player, e))
						continue;

					// Snowballs do not drop if you just breakNaturally(), so this needs to be special parsed
					if (blockMat == Material.SNOW && useExcavator) {
						ItemStack snow = new ItemStack(Material.LEGACY_SNOW_BALL, 1 + e.getData());
						e.getWorld().dropItemNaturally(blockLoc, snow);
					}

					// If there is no enchant on the item or the block is not on the effect lists, just break
					if (enchant == null ||
							((!PowerUtils.canSilkTouchMine(blockMat) || !PowerUtils.canFortuneMine(blockMat)) && useHammer) ||
							((!PowerUtils.canFortuneDig(blockMat) || !PowerUtils.canFortuneDig(blockMat)) && useExcavator))
						e.breakNaturally(handItem);
					else {
						ItemStack drop = PowerUtils.processEnchantsAndReturnItemStack(enchant, enchantLevel, e);

						if (drop != null) {
							e.getWorld().dropItemNaturally(blockLoc, drop);
							e.setType(Material.AIR);
						}
						else
							e.breakNaturally(handItem);
					}

					// If this is set, durability will be reduced from the tool for each broken block
					if (useDurabilityPerBlock || !player.hasPermission("powermining.highdurability")) {
						if (curDur++ < maxDur)
							handItem.setDurability(curDur);
						else
							break;
					}
				}
			}
		}
	}
}

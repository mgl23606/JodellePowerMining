/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for cancelling the enchanting through repair in case the user does not have permission
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class InventoryClickListener implements Listener {

	public InventoryClickListener(@Nonnull PowerMining plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void canEnchant(InventoryClickEvent event) {
		// Ignore the event in case this is not an Anvil
		if (!(event.getInventory() instanceof AnvilInventory))
			return;

		// If the player is not trying to get the resulting item out of the anvil, ignore the event
		if (event.getSlotType() != SlotType.RESULT)
			return;

		ItemStack item = event.getInventory().getItem(0);
		ItemStack item2 = event.getInventory().getItem(1);

		if (item == null){
			return;
		}
		if (item2 == null)
			return;

		// Ignore event if the first item is not a power tool
		if (!PowerUtils.isPowerTool(item))
			return;

		// If this is not an enchanted book we need to check if it another power tool or allowed ingot
		if (item2.getType() != Material.ENCHANTED_BOOK) {
			// If the second item is an allowed ingot, let it repair
			switch(item2.getType()) {
			case IRON_INGOT:
			case GOLD_INGOT:
			case DIAMOND: 
			case NETHERITE_INGOT:
				return;
			default:
				break;
			}

			// Check if the second item is a power tool
			if (PowerUtils.isPowerTool(item2)) {
				// Second item is not enchanted, let it repair
				if (item.getEnchantments().isEmpty())
					return;
			}
			else {
				event.setCancelled(true);
				return;
			}
		}

		if (!PowerUtils.checkEnchantPermission((Player) event.getWhoClicked(), item.getType()))
			event.setCancelled(true);
	}
}

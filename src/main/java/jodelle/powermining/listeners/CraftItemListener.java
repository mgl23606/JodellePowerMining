/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for cancelling the crafting in case the user does not have permission
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftItemListener implements Listener {
	PowerMining plugin;

	public CraftItemListener(PowerMining plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void canCraft(CraftItemEvent event) {
		ItemStack resultItem = event.getRecipe().getResult();

		// Check if the item is a power tool
		if (!PowerUtils.isPowerTool(resultItem))
			return;

		// Check if the player has crafting permission for this item type
		if (!PowerUtils.checkCraftPermission((Player) event.getWhoClicked(), resultItem.getType()))
			event.setCancelled(true);
	}
}

/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for cancelling the enchanting in case the user does not have permission
 */

package jodelle.powermining.listeners;


import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantItemListener implements Listener {
	PowerMining plugin;

	public EnchantItemListener(PowerMining plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void canEnchant(EnchantItemEvent event) {
		ItemStack item = event.getItem();

		if (!PowerUtils.isPowerTool(item))
			return;

		if (!PowerUtils.checkEnchantPermission(event.getEnchanter(), item.getType()))
			event.setCancelled(true);
	}
}

/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class handles PowerTool enchanting.
 * It cancels the enchantment if the player does not have permission.
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class EnchantItemListener implements Listener {

    private final PowerMining plugin;

    public EnchantItemListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Prevents enchanting of PowerTools if the player lacks permission.
     *
     * @param event The enchantment event.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEnchantItem(@Nonnull final EnchantItemEvent event) {
        final ItemStack item = event.getItem();

        // Ignore if the item is not a PowerTool
        if (!PowerUtils.isPowerTool(item)) {
            return;
        }

        // Check permission and cancel if not allowed
        if (!PowerUtils.checkEnchantPermission(event.getEnchanter(), item.getType())) {
            event.setCancelled(true);
        }
    }
}

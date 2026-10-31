/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EnchantItemListener implements Listener {

    private final PowerMining plugin;

    public EnchantItemListener(@NotNull final PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Intercepts enchanting table actions to make sure players
     * aren't enchanting custom power tools if they lack permission.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEnchantItem(@NotNull final EnchantItemEvent event) {
        final ItemStack item = event.getItem();

        // If it's just a normal vanilla item, let it slide
        if (!PowerUtils.isPowerTool(item)) {
            return;
        }

        // Check if they have the right permission for this specific tool; if not, cancel the enchant
        if (!PowerUtils.checkEnchantPermission(event.getEnchanter(), item.getType())) {
            event.setCancelled(true);
        }
    }
}
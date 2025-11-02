/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This listener prevents unauthorized PowerTool enchanting or repairing through anvils.
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

    private final PowerMining plugin;

    public InventoryClickListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Cancels PowerTool enchanting or invalid repair actions if the player lacks permission.
     *
     * @param event Inventory click event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(@Nonnull final InventoryClickEvent event) {
        // Only handle Anvil result slot interactions
        if (!(event.getInventory() instanceof AnvilInventory) || event.getSlotType() != SlotType.RESULT) {
            return;
        }

        final ItemStack item = event.getInventory().getItem(0);
        final ItemStack item2 = event.getInventory().getItem(1);

        if (item == null || item2 == null) {
            return;
        }

        // Only process if the base item is a PowerTool
        if (!PowerUtils.isPowerTool(item)) {
            return;
        }

        // Allow repairing with valid materials
        switch (item2.getType()) {
            case IRON_INGOT:
            case GOLD_INGOT:
            case DIAMOND:
            case NETHERITE_INGOT:
                return;
            default:
                break;
        }

        // Allow combining with another PowerTool if it's a repair, not enchantment
        if (PowerUtils.isPowerTool(item2) && item.getEnchantments().isEmpty()) {
            return;
        }

        // Check permissions for enchantment or invalid combination
        if (!PowerUtils.checkEnchantPermission((Player) event.getWhoClicked(), item.getType())) {
            event.setCancelled(true);
        }
    }
}

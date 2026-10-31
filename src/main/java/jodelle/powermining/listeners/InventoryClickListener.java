/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
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
import org.jetbrains.annotations.NotNull;

public class InventoryClickListener implements Listener {

    private final PowerMining plugin;

    public InventoryClickListener(@NotNull final PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Blocks players from doing weird things with custom power tools in anvils,
     * like adding illegal enchantments or bypassing permission checks.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(@NotNull final InventoryClickEvent event) {
        // Stop right here if this isn't an anvil result slot interaction
        if (!(event.getInventory() instanceof AnvilInventory) || event.getSlotType() != SlotType.RESULT) {
            return;
        }

        // Grab whatever is sitting in the two input slots of the anvil
        final ItemStack item = event.getInventory().getItem(0);
        final ItemStack item2 = event.getInventory().getItem(1);

        if (item == null || item2 == null) {
            return;
        }

        // Only care if the main item being worked on is one of our custom PowerTools
        if (!PowerUtils.isPowerTool(item)) {
            return;
        }

        // Let players repair their tools normally using standard materials
        switch (item2.getType()) {
            case IRON_INGOT:
            case GOLD_INGOT:
            case DIAMOND:
            case NETHERITE_INGOT:
                return;
            default:
                break;
        }

        // Allow combining with another clean PowerTool for repairs (as long as no custom enchants are involved)
        if (PowerUtils.isPowerTool(item2) && item.getEnchantments().isEmpty()) {
            return;
        }

        // Ensure the person clicking is actually a player
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Finally, check if they have permission to modify/enchant this specific tool type
        if (!PowerUtils.checkEnchantPermission(player, item.getType())) {
            event.setCancelled(true);
        }
    }
}
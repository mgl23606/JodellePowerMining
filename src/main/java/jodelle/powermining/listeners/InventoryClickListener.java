package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.utils.PowerUtils;

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

/**
 * Listener for handling {@link InventoryClickEvent} in an anvil to enforce enchantment and repair permissions.
 * 
 * <p>
 * This class ensures that PowerTools can only be repaired or enchanted if the 
 * player has the necessary permissions. It prevents invalid anvil operations, 
 * such as attempting to enchant or repair PowerTools with unauthorized items.
 * </p>
 */
public class InventoryClickListener implements Listener {

    private final PowerMining plugin;

    /**
     * Constructs an {@code InventoryClickListener} and registers it as an event listener.
     * 
     * @param plugin The instance of {@link PowerMining} used for event registration.
     */
    public InventoryClickListener(@Nonnull PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Validates whether a player can enchant or repair a PowerTool in an anvil.
     * 
     * <p>
     * This method ensures that only valid PowerTools can be modified and that 
     * the player has the appropriate permissions. It cancels unauthorized anvil 
     * interactions to prevent unintended modifications.
     * </p>
     * 
     * @param event The {@link InventoryClickEvent} triggered when a player interacts with an anvil.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void canEnchant(InventoryClickEvent event) {
        // Ignore the event if this is not an Anvil
        if (!(event.getInventory() instanceof AnvilInventory))
            return;

        // If the player is not trying to get the resulting item out of the anvil, ignore the event
        if (event.getSlotType() != SlotType.RESULT)
            return;

        ItemStack item = event.getInventory().getItem(0);
        ItemStack item2 = event.getInventory().getItem(1);

        if (item == null || item2 == null) {
            return;
        }

        // Ignore event if the first item is not a power tool
        if (!PowerUtils.isPowerTool(item))
            return;

        // If this is not an enchanted book we need to check if it's another power tool or an allowed ingot
        if (item2.getType() != Material.ENCHANTED_BOOK) {
            switch (item2.getType()) {
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
            } else {
                event.setCancelled(true);
                return;
            }
        }

        if (!PowerUtils.checkEnchantPermission(plugin, (Player) event.getWhoClicked(), item.getType())) {
            event.setCancelled(true);
        }
    }
}
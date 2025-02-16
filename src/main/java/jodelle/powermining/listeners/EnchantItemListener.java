package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Listener for handling {@link EnchantItemEvent} to enforce enchantment
 * permissions.
 * 
 * <p>
 * This class ensures that players can only enchant PowerTools if they have the
 * appropriate permissions. If a player attempts to enchant a PowerTool without
 * the required permission, the enchantment process is canceled.
 * </p>
 */
public class EnchantItemListener implements Listener {

    private final PowerMining plugin;

    /**
     * Constructs an {@code EnchantItemListener} and registers it as an event
     * listener.
     * 
     * @param plugin The instance of {@link PowerMining} used for event
     *               registration.
     */
    public EnchantItemListener(@Nonnull PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Checks whether a player has permission to enchant a PowerTool.
     * 
     * <p>
     * If the item being enchanted is a PowerTool and the player lacks the
     * required permission, the enchantment event is canceled.
     * </p>
     * 
     * @param event The {@link EnchantItemEvent} triggered when a player attempts to
     *              enchant an item.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void canEnchant(EnchantItemEvent event) {
        final ItemStack item = event.getItem();

        if (!PowerUtils.isPowerTool(item)) {
            return;
        }

        if (!PowerUtils.checkEnchantPermission(plugin, event.getEnchanter(), item.getType())) {
            event.setCancelled(true);
        }
    }
}

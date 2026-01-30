package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Listener for handling {@link EnchantItemEvent} to enforce enchantment permissions.
 *
 * <p>
 * This class ensures that players can only enchant PowerTools if they have the
 * appropriate permissions. If a player attempts to enchant a PowerTool without
 * the required permission, the enchantment process is canceled.
 * </p>
 *
 * <p>
 * Null-safety note: Some Bukkit API methods are not annotated as {@code @Nonnull}
 * in all distributions. Therefore, this listener performs explicit null checks
 * to satisfy static analysis and to fail fast in unexpected situations.
 * </p>
 */
public class EnchantItemListener implements Listener {

    private final @Nonnull PowerMining plugin;

    /**
     * Constructs an {@code EnchantItemListener} and registers it as an event listener.
     *
     * @param plugin
     *         The instance of {@link PowerMining} used for event registration.
     *         Must not be {@code null}.
     */
    public EnchantItemListener(@Nonnull final PowerMining plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    /**
     * Checks whether a player has permission to enchant a PowerTool.
     *
     * <p>
     * If the item being enchanted is a PowerTool and the player lacks the required
     * permission, the enchantment event is canceled.
     * </p>
     *
     * @param event
     *         The {@link EnchantItemEvent} triggered when a player attempts to enchant an item.
     *         Must not be {@code null}.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void canEnchant(@Nonnull final EnchantItemEvent event) {
        final ItemStack item = Objects.requireNonNull(event.getItem(), "event.getItem()");
        if (!PowerUtils.isPowerTool(item)) {
            return;
        }

        final Player enchanter = Objects.requireNonNull(event.getEnchanter(), "event.getEnchanter()");
        final Material type = Objects.requireNonNull(item.getType(), "item.getType()");

        if (!PowerUtils.checkEnchantPermission(this.plugin, enchanter, type)) {
            event.setCancelled(true);
        }
    }
}

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.utils.PowerUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

/**
 * Listener for handling {@link PrepareAnvilEvent} in the PowerMining plugin.
 * 
 * <p>
 * This class manages the repair and enchanting logic for custom PowerTools when used
 * in an anvil. It ensures compatibility checks, applies enchantments from enchanted
 * books, and merges enchantments when two PowerTools of the same type are combined.
 * </p>
 */
public class AnvilRepairListener implements Listener {
    private final PowerMining plugin;

    /**
     * Constructs an {@code AnvilRepairListener} and registers it as an event listener.
     * 
     * @param plugin The instance of {@link PowerMining} used for event registration.
     */
    public AnvilRepairListener(PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles anvil interactions to determine valid repairs and enchantments.
     * 
     * <p>
     * This method ensures that only compatible PowerTools can be repaired together.
     * It prevents invalid item combinations while allowing enchantments to be applied
     * from enchanted books.
     * </p>
     * 
     * @param event The {@link PrepareAnvilEvent} triggered when an item is placed in an anvil.
     */
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);
    
        if (firstItem == null) {
            return; // No first item, do nothing
        }
    
        boolean isFirstPowerTool = PowerUtils.isPowerTool(firstItem);
        boolean isSecondPowerTool = secondItem != null && PowerUtils.isPowerTool(secondItem);
    
        // If the first item is NOT a PowerTool, let Minecraft handle everything normally
        if (!isFirstPowerTool) {
            if (isSecondPowerTool) {
                event.setResult(null); // Prevent normal tools from merging with PowerTools
            }
            return;
        }
    
        // If second item is an enchanted book, apply enchantments
        if (secondItem != null && secondItem.getType() == Material.ENCHANTED_BOOK) {
            applyEnchantments(firstItem, secondItem, event);
            return;
        }
    
        // PowerTools can ONLY be repaired with the same type of PowerTool
        if (isFirstPowerTool) {
            if (!isSecondPowerTool || !isSameToolType(firstItem, secondItem)) {
                event.setResult(null); // Prevent repairing with normal tools
                return;
            }
            combineToolEnchantments(firstItem, secondItem, event);
        }
    }        

    /**
     * Checks if two PowerTools are of the same type.
     * 
     * <p>
     * This method compares the persistent data stored in the item metadata
     * to ensure that only tools of the same type can be combined in an anvil.
     * </p>
     * 
     * @param firstItem  The first {@link ItemStack} in the anvil.
     * @param secondItem The second {@link ItemStack} in the anvil.
     * @return {@code true} if both items are PowerTools of the same type, otherwise {@code false}.
     */
    private boolean isSameToolType(ItemStack firstItem, ItemStack secondItem) {
        ItemMeta firstMeta = firstItem.getItemMeta();
        ItemMeta secondMeta = secondItem.getItemMeta();

        if (firstMeta == null || secondMeta == null) {
            return false;
        }

        NamespacedKey key = new NamespacedKey(plugin, "isPowerTool");
        PersistentDataContainer firstContainer = firstMeta.getPersistentDataContainer();
        PersistentDataContainer secondContainer = secondMeta.getPersistentDataContainer();

        String firstToolType = firstContainer.get(key, PersistentDataType.STRING);
        String secondToolType = secondContainer.get(key, PersistentDataType.STRING);

        return firstToolType != null && firstToolType.equals(secondToolType);
    }

    /**
     * Applies enchantments from an enchanted book to a PowerTool.
     * 
     * <p>
     * This method ensures that the enchantments from the book are properly transferred
     * to the tool while preserving existing custom metadata.
     * </p>
     * 
     * @param tool  The {@link ItemStack} representing the PowerTool.
     * @param book  The {@link ItemStack} representing the enchanted book.
     * @param event The {@link PrepareAnvilEvent} to modify the result item.
     */
    private void applyEnchantments(ItemStack tool, ItemStack book, PrepareAnvilEvent event) {
        ItemMeta toolMeta = tool.getItemMeta();
        ItemMeta bookMeta = book.getItemMeta();

        if (toolMeta == null || bookMeta == null || !bookMeta.hasEnchants()) {
            return; // Skip if no enchantments are present
        }

        // Copy enchantments from the book to the tool
        Map<Enchantment, Integer> enchantments = bookMeta.getEnchants();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            if (enchantment.canEnchantItem(tool)) {
                toolMeta.addEnchant(enchantment, level, true); // Apply enchantment
            }
        }

        // Preserve custom metadata
        tool.setItemMeta(toolMeta);
        event.setResult(tool);
    }

    /**
     * Merges enchantments and repairs durability when two PowerTools are combined.
     * 
     * <p>
     * This method ensures that enchantments are merged correctly according to Minecraft's
     * anvil mechanics, and that the tool's durability is partially restored.
     * </p>
     * 
     * @param baseTool     The base {@link ItemStack} in the anvil.
     * @param additionTool The additional {@link ItemStack} used for repair.
     * @param event        The {@link PrepareAnvilEvent} to modify the result item.
     */
    private void combineToolEnchantments(ItemStack baseTool, ItemStack additionTool, PrepareAnvilEvent event) {
        ItemMeta baseMeta = baseTool.getItemMeta();
        ItemMeta additionMeta = additionTool.getItemMeta();
    
        if (baseMeta == null || additionMeta == null) {
            return; // Skip if metadata is missing
        }
    
        ItemStack resultTool = baseTool.clone();
        ItemMeta resultMeta = resultTool.getItemMeta();
    
        // Combine enchantments
        Map<Enchantment, Integer> additionEnchants = additionMeta.getEnchants();
        for (Map.Entry<Enchantment, Integer> entry : additionEnchants.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
    
            if (resultMeta.hasEnchant(enchantment)) {
                int currentLevel = resultMeta.getEnchantLevel(enchantment);
                if (currentLevel == level && currentLevel < enchantment.getMaxLevel()) {
                    resultMeta.addEnchant(enchantment, currentLevel + 1, true);
                } else if (currentLevel < level) {
                    resultMeta.addEnchant(enchantment, level, true);
                }
            } else {
                resultMeta.addEnchant(enchantment, level, true);
            }
        }
    
        // Repair durability
        if (resultMeta instanceof Damageable) {
            Damageable damageable = (Damageable) resultMeta;
            int baseDamage = ((Damageable) baseMeta).getDamage();
            int repairAmount = additionTool.getType().getMaxDurability() / 2; // Repairs up to 50% of max durability
    
            damageable.setDamage(Math.max(0, baseDamage - repairAmount)); // Prevents negative damage
        }
    
        // Apply changes
        resultTool.setItemMeta(resultMeta);
        event.setResult(resultTool);
    }
}
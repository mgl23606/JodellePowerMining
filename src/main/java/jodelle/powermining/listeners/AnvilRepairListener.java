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

public class AnvilRepairListener implements Listener {
    private final PowerMining plugin;

    public AnvilRepairListener(PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);

        if (firstItem == null || secondItem == null) {
            return; // No operation if either slot is empty
        }

        boolean isFirstPowerTool = PowerUtils.isPowerTool(firstItem);
        boolean isSecondPowerTool = PowerUtils.isPowerTool(secondItem);

        // Handle Repair Restrictions
        if (isFirstPowerTool != isSecondPowerTool || (isFirstPowerTool && !isSameToolType(firstItem, secondItem))) {
            if (secondItem.getType() != Material.ENCHANTED_BOOK) {
                event.setResult(null); // Prevent incompatible repairs
                return;
            }
        }

        // Handle Enchanting with Enchanted Books
        if (secondItem.getType() == Material.ENCHANTED_BOOK) {
            applyEnchantments(firstItem, secondItem, event);
        } else if (isFirstPowerTool && isSecondPowerTool) {
            // Combine enchantments if both are PowerTools
            combineToolEnchantments(firstItem, secondItem, event);
        } else {
            event.setResult(null); // Default case to prevent unintended behavior
        }
    }

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
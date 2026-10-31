/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CraftItemListener implements Listener {

    private final PowerMining plugin;
    private final DebuggingMessages debuggingMessages;
    private static final boolean DEBUG = true;

    public CraftItemListener(@NotNull final PowerMining plugin) {
        this.plugin = plugin;
        this.debuggingMessages = plugin.getDebuggingMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Intercepts crafting table events to make sure players are building custom
     * power tools correctly, have the right permissions, and transfers ingredient enchants over.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCraftItem(@NotNull final CraftItemEvent event) {
        final HumanEntity crafter = event.getWhoClicked();

        // Safety check: ensure there's actually a recipe and result item
        if (event.getRecipe() == null) return;
        final ItemStack result = event.getRecipe().getResult();
        final ItemMeta meta = result.getItemMeta();

        // Run basic sanity and permission checks
        if (meta == null || basicVerifications(event, result, meta)) {
            debuggingMessages.sendConsoleMessage(DEBUG, ChatColor.BLUE + "Basic verifications failed.");
            return;
        }

        // Identify which custom power tool they are trying to craft
        final String powerToolName = getPowerToolName(meta);
        final CraftingInventory inventory = event.getInventory();
        final ItemStack[] matrix = inventory.getMatrix();
        final ItemStack[] expectedRecipe = getExpectedRecipe(powerToolName);

        // Verify that the items placed in the crafting grid match the expected recipe layout
        if (!checkCraftingMatrix(matrix, expectedRecipe, crafter)) {
            debuggingMessages.sendConsoleMessage(DEBUG, ChatColor.BLUE + "Recipe validation failed.");
            event.setCancelled(true);
            return;
        }

        // Consume items correctly and carry over any enchantments from the ingredients
        updateCraftingMatrix(inventory, matrix, expectedRecipe);
    }

    /**
     * Handles custom ingredient consumption and merges any enchantments
     * from the input materials directly into the final crafted tool.
     */
    private void updateCraftingMatrix(@NotNull final CraftingInventory inventory,
                                      @NotNull final ItemStack[] matrix,
                                      @NotNull final ItemStack[] expectedRecipe) {

        final ItemStack result = inventory.getResult();
        if (result == null) return;

        for (int i = 0; i < matrix.length; i++) {
            final ItemStack input = matrix[i];
            final ItemStack expected = expectedRecipe[i];

            if (input == null || expected == null) continue;

            int newAmount = input.getAmount() - (expected.getAmount() - 1);
            input.setAmount(Math.max(newAmount, 0));

            // Carry over any enchantments existing on the input items into the new tool
            for (Map.Entry<Enchantment, Integer> entry : input.getEnchantments().entrySet()) {
                result.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Looks up the correct expected recipe grid array based on the power tool category.
     */
    @NotNull
    private ItemStack[] getExpectedRecipe(@NotNull final String powerToolName) {
        ItemStack[] expectedRecipe = null;

        if (Reference.HAMMERS.contains(powerToolName)) {
            expectedRecipe = Reference.HAMMER_CRAFTING_RECIPES.get(powerToolName);
        } else if (Reference.EXCAVATORS.contains(powerToolName)) {
            expectedRecipe = Reference.EXCAVATOR_CRAFTING_RECIPES.get(powerToolName);
        } else if (Reference.PLOWS.contains(powerToolName)) {
            expectedRecipe = Reference.PLOW_CRAFTING_RECIPES.get(powerToolName);
        }

        Validate.notNull(expectedRecipe, "Expected recipe not found for PowerTool: " + powerToolName);
        return expectedRecipe;
    }

    /**
     * Reads the unique power tool identifier stored in the item's PersistentDataContainer.
     */
    @NotNull
    private String getPowerToolName(@NotNull final ItemMeta meta) {
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(plugin, "isPowerTool");
        final String name = container.get(key, PersistentDataType.STRING);

        Validate.notNull(name, "PowerTool name missing from PersistentDataContainer");
        return name;
    }

    /**
     * Runs initial checks to see if the item is a power tool and whether the player has crafting rights.
     */
    private boolean basicVerifications(@NotNull final CraftItemEvent event,
                                       @NotNull final ItemStack resultItem,
                                       @Nullable final ItemMeta meta) {

        // Ignore standard vanilla items
        if (!PowerUtils.isPowerTool(resultItem)) {
            debuggingMessages.sendConsoleMessage(DEBUG, ChatColor.BLUE + "Item is not a PowerTool.");
            return true;
        }

        // Ensure the person crafting is a player
        if (!(event.getWhoClicked() instanceof Player player)) {
            return true;
        }

        // Check if they have the specific crafting permission for this tool type
        if (!PowerUtils.checkCraftPermission(player, resultItem.getType())) {
            debuggingMessages.sendConsoleMessage(DEBUG, ChatColor.BLUE + "Player lacks crafting permission.");
            player.sendMessage(ChatColor.RED + "[JodellePowerMining] You don't have permission to craft this PowerTool.");
            event.setCancelled(true);
            return true;
        }

        Validate.notNull(meta, "ItemMeta must not be null for PowerTool crafting");
        return false;
    }

    /**
     * Compares the player's crafting grid against the required quantities in the recipe.
     */
    private boolean checkCraftingMatrix(@NotNull final ItemStack[] matrix,
                                        @NotNull final ItemStack[] expectedRecipe,
                                        @NotNull final HumanEntity crafter) {

        for (int i = 0; i < matrix.length; i++) {
            final ItemStack input = matrix[i];
            final ItemStack expected = expectedRecipe[i];

            if (input == null || expected == null) continue;

            if (input.getAmount() < expected.getAmount()) {
                crafter.sendMessage(ChatColor.RED + "[JodellePowerMining] Not enough " +
                        expected.getType().name().toLowerCase().replace('_', ' ') + " in the recipe!");
                return false;
            }
        }
        return true;
    }
}
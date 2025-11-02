/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for validating PowerTool crafting recipes
 * and cancelling the crafting process if the player lacks permission or
 * uses an invalid recipe.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CraftItemListener implements Listener {

    private final PowerMining plugin;
    private final DebuggingMessages debuggingMessages;
    private static final boolean DEBUG = true;

    public CraftItemListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        this.debuggingMessages = plugin.getDebuggingMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the CraftItemEvent — validates recipe, permissions, and enchantment transfer.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCraftItem(@Nonnull final CraftItemEvent event) {
        final HumanEntity crafter = event.getWhoClicked();
        final ItemStack result = event.getRecipe().getResult();
        final ItemMeta meta = result.getItemMeta();

        if (meta == null || basicVerifications(event, result, meta)) {
            debuggingMessages.sendConsoleMessage(DEBUG, ChatColor.BLUE + "Basic verifications failed.");
            return;
        }

        final String powerToolName = getPowerToolName(meta);
        final CraftingInventory inventory = event.getInventory();
        final ItemStack[] matrix = inventory.getMatrix();
        final ItemStack[] expectedRecipe = getExpectedRecipe(powerToolName);

        if (!checkCraftingMatrix(matrix, expectedRecipe, crafter)) {
            debuggingMessages.sendConsoleMessage(DEBUG, ChatColor.BLUE + "Recipe validation failed.");
            event.setCancelled(true);
            return;
        }

        // Update crafting grid & merge enchantments from input items
        updateCraftingMatrix(inventory, matrix, expectedRecipe);
    }

    /**
     * Reduces crafting material amounts and merges enchantments from ingredients into the result.
     */
    private void updateCraftingMatrix(@Nonnull final CraftingInventory inventory,
                                      @Nonnull final ItemStack[] matrix,
                                      @Nonnull final ItemStack[] expectedRecipe) {

        final ItemStack result = inventory.getResult();
        if (result == null) return;

        for (int i = 0; i < matrix.length; i++) {
            final ItemStack input = matrix[i];
            final ItemStack expected = expectedRecipe[i];

            if (input == null || expected == null) continue;

            int newAmount = input.getAmount() - (expected.getAmount() - 1);
            input.setAmount(Math.max(newAmount, 0));

            // Merge enchantments from input items into result
            for (Map.Entry<Enchantment, Integer> entry : input.getEnchantments().entrySet()) {
                result.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Retrieves the expected recipe matrix for a PowerTool.
     */
    @NotNull
    private ItemStack[] getExpectedRecipe(@Nonnull final String powerToolName) {
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
     * Reads the PowerTool name from the PersistentDataContainer of the result item.
     */
    @NotNull
    private String getPowerToolName(@Nonnull final ItemMeta meta) {
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey(plugin, "isPowerTool");
        final String name = container.get(key, PersistentDataType.STRING);

        Validate.notNull(name, "PowerTool name missing from PersistentDataContainer");
        return name;
    }

    /**
     * Verifies whether crafting should continue (permission and validity checks).
     */
    private boolean basicVerifications(@Nonnull final CraftItemEvent event,
                                       @Nonnull final ItemStack resultItem,
                                       @Nullable final ItemMeta meta) {

        // Ignore non-PowerTool crafts
        if (!PowerUtils.isPowerTool(resultItem)) {
            debuggingMessages.sendConsoleMessage(DEBUG, ChatColor.BLUE + "Item is not a PowerTool.");
            return true;
        }

        final Player player = (Player) event.getWhoClicked();

        // Permission check
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
     * Verifies that the crafting grid matches the expected recipe and quantities.
     */
    private boolean checkCraftingMatrix(@Nonnull final ItemStack[] matrix,
                                        @Nonnull final ItemStack[] expectedRecipe,
                                        @Nonnull final HumanEntity crafter) {

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

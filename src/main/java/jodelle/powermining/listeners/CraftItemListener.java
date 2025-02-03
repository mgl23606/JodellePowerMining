package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
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
import java.util.Objects;

public class CraftItemListener implements Listener {
    private final PowerMining plugin;
    private final DebuggingMessages debuggingMessages;

    public CraftItemListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        debuggingMessages = plugin.getDebuggingMessages();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void canCraft(CraftItemEvent event) {
        final HumanEntity whoClicked = event.getWhoClicked();
        final ItemStack resultItem = event.getRecipe().getResult();
        final ItemMeta itemMeta = resultItem.getItemMeta();

        if (basicVerifications(event, resultItem, itemMeta)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "Verifications not ok");
            return;
        }

        final String powerToolName = getPowerToolName(itemMeta);
        final CraftingInventory inventory = event.getInventory();
        final ItemStack[] matrix = inventory.getMatrix();
        final ItemStack[] expectedRecipe = getExpectedRecipe(powerToolName);

        if (!checkCraftingMatrix(matrix, expectedRecipe, whoClicked)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "Recipe not ok");
            event.setCancelled(true);
            return;
        }

        updateCraftingMatrix(inventory, matrix, expectedRecipe);
    }

    private void updateCraftingMatrix(@Nonnull final CraftingInventory inventory, @Nonnull final ItemStack[] matrix,
            @Nonnull final ItemStack[] expectedRecipe) {
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] != null && expectedRecipe[i] != null) {
                int currentAmount = matrix[i].getAmount();
                int requiredAmount = expectedRecipe[i].getAmount();

                if (currentAmount >= requiredAmount) {
                    matrix[i].setAmount(currentAmount - requiredAmount);
                } else {
                    matrix[i].setAmount(0); // Ensures complete removal
                }
            }
        }
    }

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

        Objects.requireNonNull(expectedRecipe, "Expected recipe cannot be null");

        return expectedRecipe;
    }

    @NotNull
    private String getPowerToolName(@Nonnull ItemMeta itemMeta) {
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");
        String powerToolName = container.get(isPowerTool, PersistentDataType.STRING);

        Objects.requireNonNull(powerToolName, "Power tool name cannot be null");

        return powerToolName;
    }

    private boolean basicVerifications(@Nonnull CraftItemEvent event, @Nonnull ItemStack resultItem,
            @Nullable ItemMeta itemMeta) {
        if (!PowerUtils.isPowerTool(resultItem)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "The item is not a PowerTool.");
            return true;
        }

        if (!PowerUtils.checkCraftPermission(plugin, (Player) event.getWhoClicked(), resultItem.getType())) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "The player doesn't have permissions");
            event.setCancelled(true);
        }

        Objects.requireNonNull(itemMeta, "ItemMeta cannot be null");

        return false;
    }

    private boolean checkCraftingMatrix(@Nonnull final ItemStack[] matrix, @Nonnull final ItemStack[] expectedRecipe,
            @Nonnull final HumanEntity whoClicked) {
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] != null && expectedRecipe[i] != null) {
                if (matrix[i].getAmount() < expectedRecipe[i].getAmount()) {
                    debuggingMessages
                            .sendConsoleMessage(ChatColor.RED + "You didn't add enough" + expectedRecipe[i].getType());
                    whoClicked.sendMessage(plugin.getLangFile().getMessage(
                            "not_enough_items",
                            "&cYou didn't add enough %item%",
                            true).replace("%item%", expectedRecipe[i].getType().toString()));
                    return false;
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        // Skip non-power tool items to avoid interfering with standard crafting
        if (!PowerUtils.isPowerTool(event.getRecipe().getResult())) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);

                if (item != null && item.getType().isItem() && !item.getType().isEdible()) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        inventory.setItem(i, null);
                    }
                }
            }
        }, 1L);
    }
}

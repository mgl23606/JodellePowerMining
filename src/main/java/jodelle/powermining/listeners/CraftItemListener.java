package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Listener for handling {@link CraftItemEvent} when crafting PowerTools.
 *
 * <p>
 * This class validates PowerTool crafting by checking permissions and ensuring
 * the crafting
 * matrix matches the configured expected recipe (including required amounts).
 * If a condition is not met, the crafting event is cancelled.
 * </p>
 *
 * <p>
 * The crafting matrix is updated to consume the required amounts if crafting is
 * allowed.
 * </p>
 */
public class CraftItemListener implements Listener {

    private final @Nonnull PowerMining plugin;
    private final @Nonnull DebuggingMessages debuggingMessages;

    /**
     * Creates a new listener instance and registers it to Bukkit's event system.
     *
     * @param plugin The plugin instance. Must not be {@code null}.
     * @throws NullPointerException if {@code plugin} is {@code null}
     */
    public CraftItemListener(@Nonnull final PowerMining plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.debuggingMessages = Objects.requireNonNull(plugin.getDebuggingMessages(), "debuggingMessages");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Validates whether a PowerTool crafting attempt is allowed.
     *
     * <p>
     * This method performs all pre-crafting checks required to determine whether
     * the current crafting matrix matches the expected PowerTool recipe and whether
     * the crafting entity has the required permissions.
     * </p>
     *
     * <p>
     * The validation includes:
     * </p>
     * <ul>
     * <li>Ensuring the crafted result is a PowerTool</li>
     * <li>Ensuring the clicker is a {@link Player}</li>
     * <li>Checking crafting permissions for the resulting tool</li>
     * <li>Validating the crafting matrix against the expected recipe</li>
     * </ul>
     *
     * <p>
     * If any validation step fails, the crafting event is cancelled and appropriate
     * feedback is sent to the player.
     * </p>
     *
     * <p>
     * This method does <strong>not</strong> modify the crafting matrix. Ingredient
     * consumption is handled separately in {@link #onCraftItem(CraftItemEvent)}.
     * </p>
     *
     * @param event
     *              The {@link CraftItemEvent} triggered when a player attempts to
     *              craft
     *              an item.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void canCraft(@Nonnull final CraftItemEvent event) {
        final @Nonnull HumanEntity whoClicked = Objects.requireNonNull(event.getWhoClicked(), "whoClicked");

        // Recipe should be present for CraftItemEvent, but keep it defensive for null
        // analysis.
        final ItemStack resultItem = Objects.requireNonNull(event.getRecipe(), "recipe").getResult();
        final @Nullable ItemMeta itemMeta = resultItem.getItemMeta();

        if (basicVerifications(event, resultItem, itemMeta)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "Verifications not ok");
            return;
        }

        // From here on, itemMeta is guaranteed to be non-null due to
        // basicVerifications()
        final @Nonnull ItemMeta safeMeta = Objects.requireNonNull(itemMeta, "ItemMeta cannot be null here");

        final @Nonnull String powerToolName = getPowerToolName(safeMeta);
        debuggingMessages.sendConsoleMessage(ChatColor.YELLOW + "[CraftCheck] powerToolName=" + powerToolName);
        final @Nonnull CraftingInventory inventory = Objects.requireNonNull(event.getInventory(), "crafting inventory");

        final @Nonnull ItemStack[] matrix = Objects.requireNonNull(inventory.getMatrix(),
                "Crafting matrix cannot be null");
        final @Nonnull ItemStack[] expectedRecipe = getExpectedRecipe(powerToolName);

        if (!checkCraftingMatrix(matrix, expectedRecipe, whoClicked)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "Recipe not ok");
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Updates the crafting matrix by reducing material amounts as required by the
     * expected recipe.
     *
     * <p>
     * Slots are set to {@code null} when the resulting amount becomes 0.
     * The updated matrix is written back into the {@link CraftingInventory}.
     * </p>
     *
     * @param inventory      The crafting inventory. Must not be {@code null}.
     * @param matrix         The current crafting matrix (length 9). Must not be
     *                       {@code null}.
     * @param expectedRecipe The expected recipe matrix (length 9). Must not be
     *                       {@code null}.
     */
    private void updateCraftingMatrix(
            @Nonnull final CraftingInventory inventory,
            @Nonnull final ItemStack[] matrix,
            @Nonnull final ItemStack[] expectedRecipe) {
        for (int i = 0; i < matrix.length; i++) {
            final @Nullable ItemStack current = matrix[i];
            final @Nullable ItemStack expected = expectedRecipe[i];

            if (current == null || expected == null) {
                continue;
            }

            final int currentAmount = current.getAmount();
            final int requiredAmount = expected.getAmount();

            final int newAmount = currentAmount - requiredAmount;
            if (newAmount > 0) {
                current.setAmount(newAmount);
            } else {
                matrix[i] = null; // Ensure complete removal
            }
        }

        inventory.setMatrix(matrix);
    }

    /**
     * Retrieves and normalizes the expected crafting recipe for a given PowerTool.
     *
     * <p>
     * The returned recipe array represents the exact crafting matrix required to
     * craft the specified PowerTool, including the required material amounts.
     * </p>
     *
     * <p>
     * <strong>Normalization rule:</strong><br>
     * Any {@link ItemStack} that is {@code null}, has a {@link Material#AIR} type,
     * or has an amount less than or equal to {@code 0} is treated as an empty
     * crafting slot and normalized to {@code null}.
     * </p>
     *
     * <p>
     * This normalization is required because Bukkit/Paper represents empty
     * crafting slots as {@code null}, not as {@link Material#AIR}. Without this
     * step, recipes that define empty slots using AIR would incorrectly be
     * interpreted as requiring an AIR item, causing false validation errors
     * such as "You didn't add enough AIR".
     * </p>
     *
     * @param powerToolName
     *                      The unique identifier of the PowerTool being crafted.
     *                      Must not be {@code null}.
     *
     * @return
     *         A non-null {@link ItemStack} array of length 9 representing the
     *         normalized expected crafting recipe.
     *
     * @throws IllegalStateException
     *                               If no expected recipe exists for the given
     *                               PowerTool name.
     */
    private @Nonnull ItemStack[] getExpectedRecipe(@Nonnull final String powerToolName) {
        final @Nullable ItemStack[] expectedRecipeRaw;

        if (Reference.HAMMERS.contains(powerToolName)) {
            expectedRecipeRaw = Reference.HAMMER_CRAFTING_RECIPES.get(powerToolName);
        } else if (Reference.EXCAVATORS.contains(powerToolName)) {
            expectedRecipeRaw = Reference.EXCAVATOR_CRAFTING_RECIPES.get(powerToolName);
        } else if (Reference.PLOWS.contains(powerToolName)) {
            expectedRecipeRaw = Reference.PLOW_CRAFTING_RECIPES.get(powerToolName);
        } else {
            expectedRecipeRaw = null;
        }

        if (expectedRecipeRaw == null) {
            throw new IllegalStateException("Expected recipe cannot be null for PowerTool: " + powerToolName);
        }

        // Normalize: treat AIR (or amount <= 0) as "empty slot" => null
        final ItemStack[] normalized = expectedRecipeRaw.clone();
        for (int i = 0; i < normalized.length; i++) {
            final ItemStack it = normalized[i];
            if (it == null) {
                continue;
            }
            if (it.getType() == Material.AIR || it.getAmount() <= 0) {
                normalized[i] = null;
            }
        }

        return normalized;
    }

    /**
     * Extracts the PowerTool name from the item's metadata.
     *
     * @param itemMeta The {@link ItemMeta} containing the PowerTool's persistent
     *                 data. Must not be {@code null}.
     * @return The non-null PowerTool name stored in persistent data.
     * @throws IllegalStateException If the persistent data does not contain a
     *                               PowerTool name.
     */
    private @Nonnull String getPowerToolName(@Nonnull final ItemMeta itemMeta) {
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        final NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");

        final @Nullable String powerToolName = container.get(isPowerTool, PersistentDataType.STRING);
        if (powerToolName == null || powerToolName.isBlank()) {
            throw new IllegalStateException("Power tool name cannot be null/blank in persistent data.");
        }

        return powerToolName;
    }

    /**
     * Performs basic validations before allowing a PowerTool to be crafted.
     *
     * <p>
     * - Verifies the crafted result is a PowerTool.
     * - Ensures the clicker is a {@link Player}.
     * - Checks crafting permissions for the tool material.
     * - Ensures the result item has non-null {@link ItemMeta}.
     * </p>
     *
     * @param event      The craft event. Must not be {@code null}.
     * @param resultItem The resulting item from the recipe. Must not be
     *                   {@code null}.
     * @param itemMeta   The meta of the resulting item (may be {@code null}
     *                   depending on the item).
     * @return {@code true} if crafting should be denied (and execution should
     *         stop), {@code false} otherwise.
     */
    private boolean basicVerifications(
            @Nonnull final CraftItemEvent event,
            @Nonnull final ItemStack resultItem,
            @Nullable final ItemMeta itemMeta) {
        if (!PowerUtils.isPowerTool(resultItem)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "The item is not a PowerTool.");
            return true;
        }

        final HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "Crafting clicker is not a Player.");
            event.setCancelled(true);
            return true;
        }

        final Player player = (Player) clicker;

        final @Nonnull Material material = Objects.requireNonNull(resultItem.getType(), "resultItem material");
        if (!PowerUtils.checkCraftPermission(plugin, player, material)) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "The player doesn't have permissions");
            event.setCancelled(true);
            return true; // Stop processing
        }

        if (itemMeta == null) {
            debuggingMessages.sendConsoleMessage(ChatColor.BLUE + "ItemMeta is null.");
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    /**
     * Determines whether a crafting slot should be treated as empty.
     *
     * <p>
     * A slot is considered empty if the {@link ItemStack} is {@code null},
     * has a {@link Material#AIR} type, or has an amount less than or equal to
     * {@code 0}.
     * </p>
     *
     * <p>
     * This helper exists to unify empty-slot handling across different
     * Bukkit/Paper versions, where empty crafting slots may be represented
     * either as {@code null} or as an AIR {@link ItemStack}.
     * </p>
     *
     * @param item
     *             The {@link ItemStack} to check.
     *             May be {@code null}.
     *
     * @return
     *         {@code true} if the slot should be treated as empty;
     *         {@code false} otherwise.
     */
    private static boolean isEmptySlot(@Nullable ItemStack item) {
        return item == null
                || item.getType() == Material.AIR
                || item.getAmount() <= 0;
    }

    /**
     * Creates a human-readable description of an {@link ItemStack} for debugging
     * purposes.
     *
     * <p>
     * This method is intended for diagnostic output only and should not be used
     * for gameplay logic. It safely handles {@code null} values and returns a
     * concise textual representation of the item type and amount.
     * </p>
     *
     * <p>
     * Example outputs:
     * </p>
     * <ul>
     * <li>{@code "DIAMOND x3"}</li>
     * <li>{@code "AIR x0"}</li>
     * <li>{@code "null"}</li>
     * </ul>
     *
     * @param item
     *             The {@link ItemStack} to describe.
     *             May be {@code null}.
     *
     * @return
     *         A non-null string describing the given item.
     */
    private static @Nonnull String describe(@Nullable ItemStack item) {
        // English comments as requested
        if (item == null) {
            return "null";
        }
        return item.getType() + " x" + item.getAmount();
    }

    /**
     * Validates whether the current crafting matrix matches the expected recipe.
     *
     * <p>
     * This method iterates over each slot of the crafting matrix and compares it
     * against the normalized expected recipe. The validation enforces the following
     * rules:
     * </p>
     * <ul>
     * <li>Slots where the expected recipe is {@code null} must be empty.</li>
     * <li>Slots where an item is expected must contain a non-empty
     * {@link ItemStack}
     * of the correct {@link Material}.</li>
     * <li>Each required slot must contain at least the required amount.</li>
     * </ul>
     *
     * <p>
     * <strong>Empty slot handling:</strong><br>
     * A crafting slot is considered empty if the {@link ItemStack} is {@code null},
     * has a {@link Material#AIR} type, or has an amount less than or equal to
     * {@code 0}. This behavior is centralized in {@link #isEmptySlot(ItemStack)}
     * to account for differences in how Bukkit/Paper versions represent empty
     * crafting slots.
     * </p>
     *
     * <p>
     * <strong>Null-safety note:</strong><br>
     * After a slot has been verified as non-empty using {@code isEmptySlot(...)} ,
     * {@link Objects#requireNonNull(Object, String)} is used to explicitly assert
     * non-nullability of the {@link ItemStack}. This improves static analysis
     * accuracy and documents the method's assumptions for maintainers.
     * </p>
     *
     * <p>
     * If any validation rule fails, an appropriate feedback message is sent to the
     * crafting entity and the method returns {@code false}.
     * </p>
     *
     * @param matrix
     *                       The current crafting matrix provided by the
     *                       {@link CraftingInventory}.
     *                       Must not be {@code null}.
     *
     * @param expectedRecipe
     *                       The normalized expected crafting recipe.
     *                       Empty slots must be represented as {@code null}.
     *
     * @param whoClicked
     *                       The entity attempting to craft the item.
     *                       Must not be {@code null}.
     *
     * @return
     *         {@code true} if the crafting matrix exactly matches the expected
     *         recipe; {@code false} otherwise.
     */
    private boolean checkCraftingMatrix(
            @Nonnull final ItemStack[] matrix,
            @Nonnull final ItemStack[] expectedRecipe,
            @Nonnull final HumanEntity whoClicked) {

        for (int i = 0; i < matrix.length; i++) {
            final @Nullable ItemStack current = matrix[i];
            final @Nullable ItemStack expected = expectedRecipe[i];

            // Slot must be empty
            if (expected == null) {
                if (!isEmptySlot(current)) {
                    // English comments as requested
                    debuggingMessages.sendConsoleMessage(ChatColor.RED
                            + "[CraftCheck] Slot " + i + " should be EMPTY but is " + describe(current));

                    whoClicked.sendMessage(plugin.getLangFile().getMessage(
                            "invalid_recipe",
                            "&cInvalid crafting recipe.",
                            true));
                    return false;
                }
                continue;
            }

            // Slot must contain an item
            if (isEmptySlot(current)) {
                // English comments as requested
                debuggingMessages.sendConsoleMessage(ChatColor.RED
                        + "[CraftCheck] Slot " + i + " is EMPTY but expected " + describe(expected));

                whoClicked.sendMessage(plugin.getLangFile().getMessage(
                        "not_enough_items",
                        "&cYou didn't add enough %item%",
                        true).replace("%item%", expected.getType().toString()));
                return false;
            }

            // Explicitly assert non-null for static analysis
            final @Nonnull ItemStack nonNullCurrent = Objects.requireNonNull(current,
                    "Current ItemStack must not be null here");

            // Wrong material
            if (nonNullCurrent.getType() != expected.getType()) {
                // English comments as requested
                debuggingMessages.sendConsoleMessage(ChatColor.RED
                        + "[CraftCheck] Slot " + i + " wrong type. Current="
                        + describe(nonNullCurrent) + " Expected=" + describe(expected));

                whoClicked.sendMessage(plugin.getLangFile().getMessage(
                        "invalid_recipe",
                        "&cInvalid crafting recipe.",
                        true));
                return false;
            }

            // Not enough amount
            if (nonNullCurrent.getAmount() < expected.getAmount()) {
                // English comments as requested
                debuggingMessages.sendConsoleMessage(ChatColor.RED
                        + "[CraftCheck] Slot " + i + " not enough amount. Current="
                        + describe(nonNullCurrent) + " Expected=" + describe(expected));

                debuggingMessages.sendConsoleMessage(ChatColor.RED + "You didn't add enough " + expected.getType());
                whoClicked.sendMessage(plugin.getLangFile().getMessage(
                        "not_enough_items",
                        "&cYou didn't add enough %item%",
                        true).replace("%item%", expected.getType().toString()));
                return false;
            }
        }
        return true;
    }

    /**
     * Handles post-crafting logic for PowerTool recipes.
     *
     * <p>
     * This listener runs after Bukkit has successfully processed a crafting action.
     * It is responsible for consuming the exact ingredient amounts required by the
     * PowerTool recipe without interfering with Bukkit's internal crafting logic.
     * </p>
     *
     * <p>
     * To avoid race conditions with the crafting update cycle, the actual matrix
     * manipulation is executed one tick later using the Bukkit scheduler.
     * </p>
     *
     * <p>
     * Only crafting actions that result in a PowerTool are processed. All other
     * crafting events are ignored.
     * </p>
     *
     * @param event
     *              The {@link CraftItemEvent} triggered after a successful crafting
     *              operation.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftItem(@Nonnull final CraftItemEvent event) {
        // Only handle PowerTool crafts
        final @Nonnull Recipe recipe = Objects.requireNonNull(event.getRecipe(), "recipe");
        final @Nonnull ItemStack recipeResult = Objects.requireNonNull(recipe.getResult(), "recipe result");

        if (!PowerUtils.isPowerTool(recipeResult)) {
            return;
        }

        final @Nonnull CraftingInventory inventory = Objects.requireNonNull(event.getInventory(), "crafting inventory");

        // Run one tick later to avoid fighting Bukkit's crafting update cycle
        Bukkit.getScheduler().runTask(plugin, () -> {
            final @Nullable ItemStack resultInSlot = inventory.getResult();
            if (resultInSlot == null || !PowerUtils.isPowerTool(resultInSlot)) {
                return;
            }

            final @Nullable ItemMeta meta = resultInSlot.getItemMeta();
            if (meta == null) {
                return;
            }

            final @Nonnull String powerToolName = getPowerToolName(meta);

            final @Nonnull ItemStack[] matrix = Objects.requireNonNull(inventory.getMatrix(), "crafting matrix");
            final @Nonnull ItemStack[] expectedRecipe = getExpectedRecipe(powerToolName);

            // Consume exactly the required amounts
            updateCraftingMatrix(inventory, matrix, expectedRecipe);

            // Force client update (helps against ghost results on some versions)
            inventory.setResult(inventory.getResult());
        });
    }
}

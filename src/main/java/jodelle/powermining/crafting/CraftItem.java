package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Base helper for creating and registering PowerTool crafting recipes and for
 * applying standardized item metadata.
 *
 * <p>
 * This class centralizes common crafting-related functionality used by the plugin:
 * </p>
 * <ul>
 *   <li>Applying localized display names and lore to PowerTool {@link ItemStack}s.</li>
 *   <li>Persistently tagging items as PowerTools via {@link org.bukkit.persistence.PersistentDataContainer}.</li>
 *   <li>Creating {@link ShapedRecipe}s from a 3x3 {@link ItemStack} ingredient grid.</li>
 *   <li>Registering recipes safely while preventing duplicates.</li>
 * </ul>
 *
 * <p>
 * Implementations can extend this class to build specific PowerTool recipe sets.
 * </p>
 */
public class CraftItem {

    /**
     * Debug logger used for console output related to crafting and recipe registration.
     */
    private final DebuggingMessages debuggingMessages;

    /**
     * Plugin instance used for namespacing keys, accessing language files, and registering recipes.
     */
    protected final PowerMining plugin; // Use PowerMining instead of JavaPlugin for language access

    /**
     * Creates a new crafting helper.
     *
     * @param plugin
     *         The plugin instance providing access to configuration, language strings,
     *         and the server recipe registry. Must not be {@code null}.
     */
    public CraftItem(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        debuggingMessages = plugin.getDebuggingMessages();
    }

    /**
     * Applies localized metadata (display name and lore) to a PowerTool item and marks it
     * persistently for later identification.
     *
     * <p>
     * The display name and lore are loaded from the language file using:
     * {@code items.<toolKey>.name} and {@code items.<toolKey>.lore}. If a key is missing,
     * fallback defaults are used. Color codes using {@code &} are translated via
     * {@link ChatColor#translateAlternateColorCodes(char, String)}.
     * </p>
     *
     * <p>
     * Additionally, this method stores the {@code toolKey} in the item's persistent data
     * container under the namespaced key {@code isPowerTool}. This allows other parts
     * of the plugin to recognize the item as a PowerTool without relying on display
     * names or lore.
     * </p>
     *
     * @param powerTool
     *         The {@link ItemStack} to modify. Must not be {@code null}.
     * @param toolKey
     *         The language/identifier key for this tool (e.g. {@code WOODEN_HAMMER}).
     *         Must not be {@code null}.
     *
     * @throws AssertionError
     *         If {@link ItemStack#getItemMeta()} returns {@code null}.
     */
    public void modifyItemMeta(@Nonnull final ItemStack powerTool, @Nonnull final String toolKey) {
        final ItemMeta powerToolMeta = powerTool.getItemMeta();
        assert powerToolMeta != null;

        // Default values if the key is missing
        String defaultName = ChatColor.YELLOW + toolKey.replace("_", " "); // "WOODEN_HAMMER" → "Wooden Hammer"
        String defaultLore = ChatColor.GRAY + "A powerful tool for crafting.";

        // Retrieve localized name and lore from the language file (with defaults)
        String itemName = plugin.getLangFile().getMessage("items." + toolKey + ".name", defaultName, false);
        String itemLore = plugin.getLangFile().getMessage("items." + toolKey + ".lore", defaultLore, false);

        // Apply color codes for formatting
        itemName = ChatColor.translateAlternateColorCodes('&', itemName);
        itemLore = ChatColor.translateAlternateColorCodes('&', itemLore);

        // Set the item name
        powerToolMeta.setDisplayName(itemName);

        // Set lore if it's not empty
        if (!itemLore.isEmpty()) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add(itemLore);
            powerToolMeta.setLore(lore);
        }

        // Setting persistent data for identification
        final NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");
        powerToolMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.STRING, toolKey);

        powerTool.setItemMeta(powerToolMeta);
    }

    /**
     * Builds a 3x3 shaped crafting recipe for the given PowerTool item.
     *
     * <p>
     * The recipe shape is fixed to:
     * </p>
     * <pre>
     * abc
     * def
     * ghi
     * </pre>
     *
     * <p>
     * Ingredients are mapped from the provided 9-slot {@code recipe} array to the
     * corresponding placeholders {@code a} through {@code i}. Any {@code null} entries
     * or entries whose material is {@link org.bukkit.Material#AIR} are treated as empty
     * slots and are not assigned.
     * </p>
     *
     * @param powerTool
     *         The resulting {@link ItemStack} crafted by this recipe. Must not be {@code null}.
     * @param name
     *         The unique recipe name used to create the {@link NamespacedKey}. Must not be {@code null}.
     * @param recipe
     *         A 9-element array representing the crafting grid from top-left to bottom-right.
     *         Must not be {@code null}. Null or AIR entries indicate empty slots.
     *
     * @return The created {@link ShapedRecipe} instance.
     *
     * @throws ArrayIndexOutOfBoundsException
     *         If {@code recipe} contains fewer than 9 elements.
     */
    protected ShapedRecipe createRecipe(@Nonnull final ItemStack powerTool, @Nonnull final String name,
            @Nonnull final ItemStack[] recipe) {
        final ShapedRecipe toolRecipe = new ShapedRecipe(new NamespacedKey(plugin, name), powerTool);
        final char[] alphabet = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i' };
        toolRecipe.shape("abc", "def", "ghi");

        for (int i = 0; i < 9; i++) {
            if (recipe[i] != null && recipe[i].getType() != org.bukkit.Material.AIR) {
                toolRecipe.setIngredient(alphabet[i], recipe[i].getType());
            }
        }
        return toolRecipe;
    }

    /**
     * Registers the given shaped recipe with the server if it is not already present.
     *
     * <p>
     * Registration is guarded by checking {@link org.bukkit.Server#getRecipe(NamespacedKey)}
     * for the recipe's key. If no existing recipe is found, the recipe is added via
     * {@link org.bukkit.Server#addRecipe(org.bukkit.inventory.Recipe)} and a debug message
     * is logged. If a recipe with the same key already exists, registration is skipped
     * and a warning is logged instead.
     * </p>
     *
     * @param recipe
     *         The {@link ShapedRecipe} to register. Must not be {@code null}.
     */
    protected void registerRecipes(@Nonnull final ShapedRecipe recipe) {
        NamespacedKey key = recipe.getKey();
        if (plugin.getServer().getRecipe(key) == null) { // Check if it exists before adding
            debuggingMessages.sendConsoleMessage(ChatColor.AQUA + "Adding Recipe: " + key.getKey());
            plugin.getServer().addRecipe(recipe);
        } else {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "Skipping duplicate recipe: " + key.getKey());
        }
    }
}
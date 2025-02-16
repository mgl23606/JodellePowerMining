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

public class CraftItem {

    private final DebuggingMessages debuggingMessages;
    protected final PowerMining plugin; // Use PowerMining instead of JavaPlugin for language access

    public CraftItem(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        debuggingMessages = plugin.getDebuggingMessages();
    }

    /**
     * Modifies the metadata of a given PowerTool item by setting its display name
     * and lore
     * based on values retrieved from the language file. If the corresponding key is
     * missing
     * in the language file, default values are used.
     * 
     * <p>
     * Additionally, this method stores a persistent data value in the item's
     * metadata
     * to mark it as a PowerTool for identification purposes.
     * </p>
     * 
     * @param powerTool The {@link ItemStack} to be modified.
     * @param toolKey   The key representing the tool in the language file, used to
     *                  retrieve
     *                  localized name and lore.
     * 
     * @throws AssertionError If the item's metadata is null.
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
     * Creates a shaped crafting recipe for the specified PowerTool.
     * 
     * <p>
     * This method generates a new {@link ShapedRecipe} using a predefined 3x3
     * crafting
     * grid pattern ("abc", "def", "ghi"). It then assigns ingredients to each slot
     * based
     * on the provided recipe array, using characters 'a' through 'i' as
     * placeholders
     * for the crafting grid.
     * </p>
     * 
     * <p>
     * Any null or air-material slots in the provided recipe array are ignored.
     * </p>
     * 
     * @param powerTool The resulting {@link ItemStack} that will be crafted using
     *                  this recipe.
     * @param name      The unique name of the recipe, used for namespacing.
     * @param recipe    An array of {@link ItemStack} representing the 3x3 crafting
     *                  grid,
     *                  where null or air values indicate empty slots.
     * 
     * @return A {@link ShapedRecipe} object representing the crafted item.
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
     * Registers a shaped crafting recipe with the server, ensuring that duplicate recipes 
     * are not added.
     * 
     * <p>
     * This method checks if a recipe with the same {@link NamespacedKey} already exists 
     * before registering it. If the recipe does not exist, it is added to the server's 
     * recipe registry, and a debug message is logged to the console. If the recipe is 
     * already present, a warning message is logged instead.
     * </p>
     * 
     * @param recipe The {@link ShapedRecipe} to be registered.
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

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
     * Modifies the PowerTool meta with name and lore from the language file
     * 
     * @param powerTool Item to be modified
     * @param toolKey   The key of the tool in the language file
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

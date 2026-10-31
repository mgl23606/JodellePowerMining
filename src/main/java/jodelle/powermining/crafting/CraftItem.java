/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CraftItem {

    private final DebuggingMessages debuggingMessages;
    protected final JavaPlugin plugin;

    public CraftItem(@NotNull final PowerMining plugin) {
        this.plugin = plugin;
        this.debuggingMessages = plugin.getDebuggingMessages();
    }

    /**
     * Modifies a power tool item's meta data, setting its custom persistent data tag, 
     * formatted display name, and lore lines.
     */
    protected void modifyItemMeta(@NotNull final ItemStack powerTool,
                                  @NotNull final String loreLine1,
                                  @NotNull final String loreLine2,
                                  @NotNull final String name) {

        ItemMeta meta = powerTool.getItemMeta();
        if (meta == null) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "[PowerMining] Failed to get ItemMeta for " + name);
            return;
        }

        // Store our unique marker in the persistent data container so the plugin recognizes it as a power tool
        NamespacedKey key = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, name);

        // Format internal names like "DIAMOND_HAMMER" into clean display names like "Diamond Hammer"
        String displayName = formatDisplayName(name);
        meta.setDisplayName(ChatColor.AQUA + displayName);

        // Attach the two-line lore description
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + loreLine1);
        lore.add(ChatColor.DARK_GRAY + loreLine2);
        meta.setLore(lore);

        powerTool.setItemMeta(meta);
    }

    /**
     * Converts internal enum strings like "DIAMOND_HAMMER" into clean Title Case format.
     */
    private String formatDisplayName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Constructs a shaped 3x3 recipe for a custom power tool from an item array.
     */
    protected ShapedRecipe createRecipe(@NotNull final ItemStack powerTool,
                                        @NotNull final String name,
                                        @NotNull final ItemStack[] recipe) {

        NamespacedKey key = new NamespacedKey(plugin, name.toLowerCase());
        ShapedRecipe shapedRecipe = new ShapedRecipe(key, powerTool);

        shapedRecipe.shape("abc", "def", "ghi");
        char[] grid = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};

        for (int i = 0; i < recipe.length && i < 9; i++) {
            if (recipe[i] != null) {
                shapedRecipe.setIngredient(grid[i], recipe[i].getType());
            }
        }

        return shapedRecipe;
    }

    /**
     * Registers the custom recipe with the server safely, catching duplicate errors if they occur.
     */
    protected void registerRecipes(@NotNull final ShapedRecipe recipe) {
        try {
            plugin.getServer().addRecipe(recipe);
            debuggingMessages.sendConsoleMessage(ChatColor.AQUA + "Registered recipe: " + recipe.getKey().getKey());
        } catch (IllegalStateException ignored) {
            debuggingMessages.sendConsoleMessage(ChatColor.YELLOW + "Skipped duplicate recipe: " + recipe.getKey().getKey());
        } catch (Exception e) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "Failed to register recipe: " + e.getMessage());
        }
    }
}
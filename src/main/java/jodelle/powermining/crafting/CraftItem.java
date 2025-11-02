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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CraftItem {

    private final DebuggingMessages debuggingMessages;
    protected final JavaPlugin plugin;

    public CraftItem(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        this.debuggingMessages = plugin.getDebuggingMessages();
    }

    /**
     * Modifies the PowerTool's meta — setting name, lore, and custom data tag.
     *
     * @param powerTool Item to modify.
     * @param loreLine1 First line of lore (flavor text).
     * @param loreLine2 Second line of lore (ability description).
     * @param name Internal name of the tool (e.g. "DIAMOND_HAMMER").
     */
    protected void modifyItemMeta(@Nonnull final ItemStack powerTool,
                                  @Nonnull final String loreLine1,
                                  @Nonnull final String loreLine2,
                                  @Nonnull final String name) {

        ItemMeta meta = powerTool.getItemMeta();
        if (meta == null) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "[PowerMining] Failed to get ItemMeta for " + name);
            return;
        }

        // Persistent Data Tag
        NamespacedKey key = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, name);

        // Format display name to "Diamond Hammer"
        String displayName = formatDisplayName(name);
        meta.setDisplayName(ChatColor.AQUA + displayName);

        // Two-line lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + loreLine1);
        lore.add(ChatColor.DARK_GRAY + loreLine2);
        meta.setLore(lore);

        powerTool.setItemMeta(meta);
    }

    /**
     * Converts internal tool names like "DIAMOND_HAMMER" to "Diamond Hammer"
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
     * Creates a ShapedRecipe from a 3x3 ItemStack array.
     */
    protected ShapedRecipe createRecipe(@Nonnull final ItemStack powerTool,
                                        @Nonnull final String name,
                                        @Nonnull final ItemStack[] recipe) {

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
     * Registers a recipe safely — ignores duplicates silently.
     */
    protected void registerRecipes(@Nonnull final ShapedRecipe recipe) {
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

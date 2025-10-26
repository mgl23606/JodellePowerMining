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

public class CraftItem {

    private final DebuggingMessages debuggingMessages;
    protected final JavaPlugin plugin;

    public CraftItem(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        debuggingMessages = plugin.getDebuggingMessages();
    }

    /**
     * Modifies the PowerTool meta, setting the custom name, the two-line lore,
     * and the Persistent Data Tag to identify it as a power tool.
     *
     * @param powerTool Item to be modified
     * @param loreLine1 The first line of lore (Flavor Text).
     * @param loreLine2 The second line of lore (Ability Description).
     * @param name The internal name of the tool (e.g., "DIAMOND_HAMMER").
     */
    protected void modifyItemMeta(@Nonnull final ItemStack powerTool, @Nonnull final String loreLine1, @Nonnull final String loreLine2, @Nonnull final String name){
        final ItemMeta powerToolMeta = powerTool.getItemMeta();

        // 1. Set Persistent Data Container (PDC)
        final NamespacedKey isPowerTool = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
        assert powerToolMeta != null;
        powerToolMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.STRING, name);

        // 2. Format Display Name - THIS IS THE CHANGE
        // Replaces underscores and then converts to Title Case (e.g., "COPPER HAMMER")
        String displayString = name.replace("_", " ").toLowerCase();

        // Capitalize the first letter of each word
        String[] words = displayString.split(" ");
        StringBuilder prettyNameBuilder = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                prettyNameBuilder.append(Character.toUpperCase(word.charAt(0)));
                prettyNameBuilder.append(word.substring(1)).append(" ");
            }
        }
        String prettyName = ChatColor.AQUA + prettyNameBuilder.toString().trim();
        powerToolMeta.setDisplayName(prettyName);

        // 3. Set the Two-Line Lore
        final ArrayList<String> lore = new ArrayList<>();
        lore.add(loreLine1);
        lore.add(loreLine2);

        powerToolMeta.setLore(lore);

        // 4. Apply the Meta
        powerTool.setItemMeta(powerToolMeta);
    }

    protected ShapedRecipe createRecipe(@Nonnull final ItemStack powerTool, @Nonnull final String name, @Nonnull final ItemStack[] recipe){

        //Initialize the recipe
        final ShapedRecipe toolRecipe = new ShapedRecipe(new NamespacedKey(plugin, name),powerTool);
        //console.sendMessage(ChatColor.AQUA + "NameSpacedKey:" + name);
        final char[] alphabet = new char[]{
                'a','b','c','d','e','f','g','h','i'
        };


        toolRecipe.shape("abc", "def", "ghi");
        for (int i = 0; i < 9; i++) {
            if (recipe[i] != null) {
                toolRecipe.setIngredient(alphabet[i], recipe[i].getType());
            }
        }

        return toolRecipe;

    }

    protected void registerRecipes(@Nonnull final ShapedRecipe recipe) {

        debuggingMessages.sendConsoleMessage(ChatColor.AQUA + "Adding Recipe:" + recipe.getKey().getKey());
        plugin.getServer().addRecipe(recipe);
    }

}

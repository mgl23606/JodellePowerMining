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
     * Modifies the PowerTool meta
     * @param powerTool Item to be modified
     * @param loreString Lore String
     * @param name Name of the tool
     */
    protected void modifyItemMeta(@Nonnull final ItemStack powerTool, @Nonnull final String loreString, @Nonnull final String name){
        final ItemMeta powerToolMeta = powerTool.getItemMeta();

        final NamespacedKey isPowerTool = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
        assert powerToolMeta != null;
        powerToolMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.STRING, name);

        final ArrayList<String> lore = new ArrayList<>();
        lore.add(loreString);

        powerToolMeta.setDisplayName(name);

        powerToolMeta.setLore(lore);

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

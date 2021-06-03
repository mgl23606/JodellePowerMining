package jodelle.powermining.crafting;


import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebugggingMessages;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CraftItem {

    private final DebugggingMessages debugggingMessages;
    protected JavaPlugin plugin;

    public CraftItem(JavaPlugin plugin) {
        this.plugin = plugin;
        debugggingMessages = new DebugggingMessages();
    }

    protected void modifyItemMeta(ItemStack powerTool, String loreString, String name){
        ItemMeta powerToolMeta = powerTool.getItemMeta();

        NamespacedKey isPowerTool = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
        assert powerToolMeta != null;
        powerToolMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.STRING, name);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(loreString);

        powerToolMeta.setDisplayName(loreString);

        powerToolMeta.setLore(lore);

        powerTool.setItemMeta(powerToolMeta);

    }

    protected ShapedRecipe createRecipe(ItemStack powerTool, String name, ItemStack[] recipe){
        //Initialize the recipe
        ShapedRecipe toolRecipe = new ShapedRecipe(new NamespacedKey(plugin, name),powerTool);
        //console.sendMessage(ChatColor.AQUA + "NameSpacedKey:" + name);
        char[] alphabet = new char[]{
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

    protected void registerRecipes(ShapedRecipe recipe) {
        debugggingMessages.sendConsoleMessage(ChatColor.AQUA + "Adding Recipe:" + recipe.getKey().getKey());
        plugin.getServer().addRecipe(recipe);
    }

}

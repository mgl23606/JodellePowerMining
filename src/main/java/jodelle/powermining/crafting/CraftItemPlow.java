package jodelle.powermining.crafting;

import jodelle.powermining.lib.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class CraftItemPlow extends CraftItem {

    public static String loreString = "PLOW!";

    public CraftItemPlow(JavaPlugin plugin) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        this.plugin = plugin;
        for(Map.Entry<String, ItemStack[]> tool : Reference.PLOW_CRAFTING_RECIPES.entrySet()){

            //key is the name of the powertool. Ex: DIAMOND_HAMMER
            //value is an array containing the recipe
            String key = tool.getKey();
            ItemStack[] value = tool.getValue();
            console.sendMessage(ChatColor.AQUA + "Creating: " + key);
            //We start by finding the position of the name on the HAMMERS array
            //With that position we can fetch the name of the minecraft item present in other array
            int i = Reference.PLOWS.indexOf(key);
            console.sendMessage(ChatColor.AQUA + String.valueOf(i));
            Material pickaxe = Reference.HOES.get(i);

            ItemStack powerTool = new ItemStack(pickaxe, 1);
            //powerTools.add(powerTool);

            modifyItemMeta(powerTool, loreString, key);

            ShapedRecipe recipe = createRecipe(powerTool, key, value);

            registerRecipes(recipe);

        }
    }

}

package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import javax.annotation.Nonnull;
import java.util.Map;

public class CraftItemPlow extends CraftItem {

    public static final String loreString = "PLOW!";

    public CraftItemPlow(@Nonnull final PowerMining plugin) {
        super(plugin);

        for(Map.Entry<String, ItemStack[]> tool : Reference.PLOW_CRAFTING_RECIPES.entrySet()){

            //key is the name of the powertool. Ex: DIAMOND_HAMMER
            //value is an array containing the recipe
            final String key = tool.getKey();
            final ItemStack[] value = tool.getValue();
            //console.sendMessage(ChatColor.AQUA + "Creating: " + key);

            //We start by finding the position of the name on the HAMMERS array
            //With that position we can fetch the name of the minecraft item present in other array

            int i = Reference.PLOWS.indexOf(key);
            //console.sendMessage(ChatColor.AQUA + String.valueOf(i));
            final Material pickaxe = Reference.HOES.get(i);

            final ItemStack powerTool = new ItemStack(pickaxe, 1);
            //powerTools.add(powerTool);

            modifyItemMeta(powerTool, loreString, key);

            final ShapedRecipe recipe = createRecipe(powerTool, key, value);

            registerRecipes(recipe);

        }
    }

}

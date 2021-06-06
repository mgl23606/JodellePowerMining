/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for creating the Hammer items and their respective crafting recipes
 */

package jodelle.powermining.crafting;


import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class CraftItemHammer extends CraftItem{

	public static String loreString = "SMASH!";

	public CraftItemHammer(PowerMining plugin) {
		super(plugin);

		for(Map.Entry<String, ItemStack[]> tool : Reference.HAMMER_CRAFTING_RECIPES.entrySet()){

			//key is the name of the powertool. Ex: DIAMOND_HAMMER
			//value is an array containing the recipe
			String key = tool.getKey();
			ItemStack[] value = tool.getValue();
			//console.sendMessage(ChatColor.AQUA + "Creating: " + key);

			//We start by finding the position of the name on the HAMMERS array
			//With that position we can fetch the name of the minecraft item present in other array
			int i = Reference.HAMMERS.indexOf(key);

			//console.sendMessage(ChatColor.AQUA + String.valueOf(i));
			Material pickaxe = Reference.PICKAXES.get(i);

			ItemStack powerTool = new ItemStack(pickaxe, 1);
			//powerTools.add(powerTool);

			modifyItemMeta(powerTool, loreString, key);

			ShapedRecipe recipe = createRecipe(powerTool, key, value);

			registerRecipes(recipe);
		}

	}






}

/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for creating the Excavator items and their respective crafting recipes
 */

package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import javax.annotation.Nonnull;
import java.util.Map;

public class CraftItemExcavator extends CraftItem {

    // --- CONSOLIDATED LORE FOR ALL EXCAVATORS ---
    // Line 1: Flavor Text (Gray, Italic)
    public static final String LORE_FLAVOR = "§8§oThe relentless earthmover.§r";
    // Line 2: Ability Description (Gold)
    public static final String LORE_ABILITY = "§6Digs a 3x3 area.§r";

    public CraftItemExcavator(@Nonnull PowerMining plugin) {
        super(plugin);

        for(Map.Entry<String, ItemStack[]> tool : Reference.EXCAVATOR_CRAFTING_RECIPES.entrySet()){

            // key is the name of the powertool. Ex: DIAMOND_EXCAVATOR
            // value is an array containing the recipe
            final String key = tool.getKey();
            final ItemStack[] value = tool.getValue();

            // We start by finding the position of the name on the EXCAVATORS array
            // With that position we can fetch the name of the minecraft item present in the SHOVELS array
            int i = Reference.EXCAVATORS.indexOf(key);

            final Material shovel = Reference.SHOVELS.get(i);

            final ItemStack powerTool = new ItemStack(shovel, 1);

            // --- APPLY BOTH LINES OF LORE ---
            modifyItemMeta(powerTool, LORE_FLAVOR, LORE_ABILITY, key);

            final ShapedRecipe recipe = createRecipe(powerTool, key, value);

            registerRecipes(recipe);
        }
    }

    /*
     * NOTE: This class assumes the base class 'CraftItem' has been updated
     * to include a 'modifyItemMeta' method that accepts three String parameters
     * for the two lines of lore (LORE_FLAVOR and LORE_ABILITY) plus the tool name (key).
     */
}
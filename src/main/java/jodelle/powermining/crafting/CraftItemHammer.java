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

import javax.annotation.Nonnull;
import java.util.Map;

public class CraftItemHammer extends CraftItem{

    // --- CONSOLIDATED LORE FOR ALL HAMMERS ---
    // Line 1: Flavor Text (Gray, Italic)
    public static final String LORE_FLAVOR = "§8§oForged to move mountains.§r";
    // Line 2: Ability Description (Gold)
    public static final String LORE_ABILITY = "§6Mines a 3x3 area.§r";

    // NOTE: The original 'public static final String loreString = "SMASH!";' is now obsolete
    // and can be removed, but we'll leave it for now and comment it out if you prefer.
    // public static final String loreString = "SMASH!";

    public CraftItemHammer(@Nonnull PowerMining plugin) {
        super(plugin);

        for(Map.Entry<String, ItemStack[]> tool : Reference.HAMMER_CRAFTING_RECIPES.entrySet()){

            // key is the name of the powertool. Ex: DIAMOND_HAMMER
            // value is an array containing the recipe
            final String key = tool.getKey();
            final ItemStack[] value = tool.getValue();

            // We start by finding the position of the name on the HAMMERS array
            // With that position we can fetch the name of the minecraft item present in the PICKAXES array
            int i = Reference.HAMMERS.indexOf(key);

            final Material pickaxe = Reference.PICKAXES.get(i);

            final ItemStack powerTool = new ItemStack(pickaxe, 1);

            // --- APPLY BOTH LINES OF LORE ---
            // Assuming modifyItemMeta is now: modifyItemMeta(ItemStack, String lore1, String lore2, String name)
            modifyItemMeta(powerTool, LORE_FLAVOR, LORE_ABILITY, key);

            final ShapedRecipe recipe = createRecipe(powerTool, key, value);

            registerRecipes(recipe);
        }
    }
}
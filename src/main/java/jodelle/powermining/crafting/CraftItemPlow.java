/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import javax.annotation.Nonnull;
import java.util.Map;

public class CraftItemPlow extends CraftItem {

    // --- CONSOLIDATED LORE FOR ALL PLOWS ---
    // Line 1: Flavor Text (Gray, Italic)
    public static final String LORE_FLAVOR = "§8§oCultivates a kingdom, quickly.§r";
    // Line 2: Ability Description (Gold)
    public static final String LORE_ABILITY = "§6Tills a 3x3 area.§r";

    // NOTE: The original 'public static final String loreString = "PLOW!";' is now obsolete
    // and can be removed.
    // public static final String loreString = "PLOW!";

    public CraftItemPlow(@Nonnull final PowerMining plugin) {
        super(plugin);

        for(Map.Entry<String, ItemStack[]> tool : Reference.PLOW_CRAFTING_RECIPES.entrySet()){

            // key is the name of the powertool. Ex: DIAMOND_PLOW
            // value is an array containing the recipe
            final String key = tool.getKey();
            final ItemStack[] value = tool.getValue();

            // We start by finding the position of the name on the PLOWS array
            // With that position we can fetch the name of the minecraft item (Hoe)
            int i = Reference.PLOWS.indexOf(key);

            final Material hoe = Reference.HOES.get(i);

            final ItemStack powerTool = new ItemStack(hoe, 1);

            // --- APPLY BOTH LINES OF LORE ---
            // Requires modifyItemMeta(ItemStack, String lore1, String lore2, String name)
            modifyItemMeta(powerTool, LORE_FLAVOR, LORE_ABILITY, key);

            final ShapedRecipe recipe = createRecipe(powerTool, key, value);

            registerRecipes(recipe);
        }
    }
}
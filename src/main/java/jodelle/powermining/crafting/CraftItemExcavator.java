/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt
 * or the GNU website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Responsible for creating the Excavator items and their respective crafting recipes.
 */

package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import javax.annotation.Nonnull;
import java.util.Map;

public class CraftItemExcavator extends CraftItem {

    // --- LORE CONSTANTS ---
    private static final String LORE_FLAVOR = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "The relentless earthmover.";
    private static final String LORE_ABILITY = ChatColor.GOLD + "Digs a 3x3 area.";

    public CraftItemExcavator(@Nonnull PowerMining plugin) {
        super(plugin);

        if (Reference.EXCAVATOR_CRAFTING_RECIPES.isEmpty()) {
            plugin.getLogger().warning("[PowerMining] No Excavator recipes found in JSON configuration.");
            return;
        }

        for (Map.Entry<String, ItemStack[]> entry : Reference.EXCAVATOR_CRAFTING_RECIPES.entrySet()) {
            String toolName = entry.getKey();
            ItemStack[] recipeArray = entry.getValue();

            // Ensure we have a matching tool in Reference
            int index = Reference.EXCAVATORS.indexOf(toolName);
            if (index < 0 || index >= Reference.SHOVELS.size()) {
                plugin.getLogger().warning("[PowerMining] Invalid Excavator mapping for tool: " + toolName);
                continue;
            }

            Material baseMaterial = Reference.SHOVELS.get(index);
            if (baseMaterial == null) {
                plugin.getLogger().warning("[PowerMining] Null base material for " + toolName);
                continue;
            }

            // Create tool instance
            ItemStack excavator = new ItemStack(baseMaterial, 1);
            modifyItemMeta(excavator, LORE_FLAVOR, LORE_ABILITY, toolName);

            // Create recipe safely
            ShapedRecipe shapedRecipe = createRecipe(excavator, toolName, recipeArray);
            registerRecipes(shapedRecipe);

            //plugin.getLogger().info(ChatColor.GREEN + "[PowerMining] Registered Excavator: " + toolName);
        }
    }

    /*
     * NOTE:
     * This class assumes the base class 'CraftItem' includes a 'modifyItemMeta'
     * method accepting (ItemStack, String, String, String).
     *
     * The recipes themselves are loaded dynamically from JSON and registered here.
     */
}

/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt
 * or the GNU website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Responsible for creating the Hammer items and their respective crafting recipes.
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

public class CraftItemHammer extends CraftItem {

    // --- LORE CONSTANTS ---
    private static final String LORE_FLAVOR = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Forged to move mountains.";
    private static final String LORE_ABILITY = ChatColor.GOLD + "Mines a 3x3 area.";

    public CraftItemHammer(@Nonnull PowerMining plugin) {
        super(plugin);

        if (Reference.HAMMER_CRAFTING_RECIPES.isEmpty()) {
            plugin.getLogger().warning("[PowerMining] No Hammer recipes found in JSON configuration.");
            return;
        }

        for (Map.Entry<String, ItemStack[]> entry : Reference.HAMMER_CRAFTING_RECIPES.entrySet()) {
            String toolName = entry.getKey();
            ItemStack[] recipeArray = entry.getValue();

            // Validate index mapping
            int index = Reference.HAMMERS.indexOf(toolName);
            if (index < 0 || index >= Reference.PICKAXES.size()) {
                plugin.getLogger().warning("[PowerMining] Invalid Hammer mapping for tool: " + toolName);
                continue;
            }

            Material pickaxeMat = Reference.PICKAXES.get(index);
            if (pickaxeMat == null) {
                plugin.getLogger().warning("[PowerMining] Null pickaxe material for " + toolName);
                continue;
            }

            // Create tool item
            ItemStack hammer = new ItemStack(pickaxeMat, 1);
            modifyItemMeta(hammer, LORE_FLAVOR, LORE_ABILITY, toolName);

            // Build and register the shaped recipe
            ShapedRecipe shapedRecipe = createRecipe(hammer, toolName, recipeArray);
            registerRecipes(shapedRecipe);

            //plugin.getLogger().info(ChatColor.GREEN + "[PowerMining] Registered Hammer: " + toolName);
        }
    }

    /*
     * NOTE:
     * This class assumes that 'CraftItem' provides 'modifyItemMeta'
     * with parameters (ItemStack, String, String, String),
     * and that recipe data is loaded dynamically from JSON.
     */
}

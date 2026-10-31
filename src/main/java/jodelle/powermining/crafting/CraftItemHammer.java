/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt
- or the GNU website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CraftItemHammer extends CraftItem {

    // Lore lines added to every custom hammer item
    private static final String LORE_FLAVOR = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Forged to move mountains.";
    private static final String LORE_ABILITY = ChatColor.GOLD + "Mines a 3x3 area.";

    public CraftItemHammer(@NotNull PowerMining plugin) {
        super(plugin);

        // Make sure hammer recipes are actually loaded from JSON before trying to build them
        if (Reference.HAMMER_CRAFTING_RECIPES.isEmpty()) {
            plugin.getLogger().warning("[PowerMining] No Hammer recipes found in JSON configuration.");
            return;
        }

        // Loop through each configured hammer recipe definition
        for (Map.Entry<String, ItemStack[]> entry : Reference.HAMMER_CRAFTING_RECIPES.entrySet()) {
            String toolName = entry.getKey();
            ItemStack[] recipeArray = entry.getValue();

            // Match the hammer name to its corresponding vanilla pickaxe tier in our reference lists
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

            // Instantiate the item, apply custom meta, name, and lore tags
            ItemStack hammer = new ItemStack(pickaxeMat, 1);
            modifyItemMeta(hammer, LORE_FLAVOR, LORE_ABILITY, toolName);

            // Build the shaped recipe and register it with the server
            ShapedRecipe shapedRecipe = createRecipe(hammer, toolName, recipeArray);
            registerRecipes(shapedRecipe);
        }
    }
}
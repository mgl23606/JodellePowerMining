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

public class CraftItemPlow extends CraftItem {

    // Lore lines added to every custom plow item
    private static final String LORE_FLAVOR = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Cultivates a kingdom, quickly.";
    private static final String LORE_ABILITY = ChatColor.GOLD + "Tills a 3x3 area.";

    public CraftItemPlow(@NotNull final PowerMining plugin) {
        super(plugin);

        // Ensure plow recipes are loaded from JSON before trying to construct them
        if (Reference.PLOW_CRAFTING_RECIPES.isEmpty()) {
            plugin.getLogger().warning("[PowerMining] No Plow recipes found in JSON configuration.");
            return;
        }

        // Loop through each configured plow recipe entry
        for (Map.Entry<String, ItemStack[]> entry : Reference.PLOW_CRAFTING_RECIPES.entrySet()) {
            String toolName = entry.getKey();
            ItemStack[] recipeArray = entry.getValue();

            // Match the plow name to its corresponding vanilla hoe tier in our reference lists
            int index = Reference.PLOWS.indexOf(toolName);
            if (index < 0 || index >= Reference.HOES.size()) {
                plugin.getLogger().warning("[PowerMining] Invalid Plow mapping for tool: " + toolName);
                continue;
            }

            Material hoeMat = Reference.HOES.get(index);
            if (hoeMat == null) {
                plugin.getLogger().warning("[PowerMining] Null hoe material for " + toolName);
                continue;
            }

            // Instantiate the item, apply custom meta, display name, and lore tags
            ItemStack plow = new ItemStack(hoeMat, 1);
            modifyItemMeta(plow, LORE_FLAVOR, LORE_ABILITY, toolName);

            // Construct the shaped recipe and register it with the server
            ShapedRecipe shapedRecipe = createRecipe(plow, toolName, recipeArray);
            registerRecipes(shapedRecipe);
        }
    }
}
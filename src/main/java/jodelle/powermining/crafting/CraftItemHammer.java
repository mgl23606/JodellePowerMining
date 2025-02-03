package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;

public class CraftItemHammer extends CraftItem {
    public CraftItemHammer(@Nonnull PowerMining plugin) {
        super(plugin);

        for (Map.Entry<String, ItemStack[]> tool : Reference.HAMMER_CRAFTING_RECIPES.entrySet()) {

            final String key = tool.getKey();
            int i = Reference.HAMMERS.indexOf(key);
            final Material pickaxe = Reference.PICKAXES.get(i);

            final ItemStack powerTool = new ItemStack(pickaxe, 1);
            
            modifyItemMeta(powerTool, key);
        }
    }
}
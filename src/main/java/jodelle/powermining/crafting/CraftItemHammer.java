package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents the crafting logic for hammer tools in the PowerMining plugin.
 * 
 * <p>
 * This class extends {@link CraftItem} and is responsible for initializing
 * and modifying the metadata of hammer tools using predefined crafting
 * recipes from {@link Reference#HAMMER_CRAFTING_RECIPES}.
 * </p>
 */
public class CraftItemHammer extends CraftItem {
    /**
     * Constructs a {@code CraftItemHammer} and initializes crafting recipes
     * for all defined hammers.
     * 
     * <p>
     * This constructor iterates through the predefined hammer crafting recipes,
     * retrieves the corresponding tool type, and modifies its item metadata using
     * {@link #modifyItemMeta(ItemStack, String)}.
     * </p>
     * 
     * @param plugin The instance of {@link PowerMining} used for accessing
     *               plugin-related functionalities.
     */
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
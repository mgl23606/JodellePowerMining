package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

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
     * <p>
     * To satisfy nullness analysis, the recipe key is explicitly validated to be non-null
     * using {@link Objects#requireNonNull(Object, String)} before being passed to methods
     * expecting {@code @Nonnull} parameters.
     * </p>
     *
     * @param plugin The instance of {@link PowerMining} used for accessing
     *               plugin-related functionalities.
     * @throws NullPointerException if a recipe key in {@link Reference#HAMMER_CRAFTING_RECIPES} is null
     */
    public CraftItemHammer(@Nonnull PowerMining plugin) {
        super(plugin);

        for (Map.Entry<String, ItemStack[]> tool : Reference.HAMMER_CRAFTING_RECIPES.entrySet()) {

            // Make nullness explicit for the compiler/null analysis
            final String key = Objects.requireNonNull(tool.getKey(), "Hammer recipe key is null");

            final int i = Reference.HAMMERS.indexOf(key);
            final Material pickaxe = Reference.PICKAXES.get(i);

            final ItemStack powerTool = new ItemStack(pickaxe, 1);

            modifyItemMeta(powerTool, key);
        }
    }
}
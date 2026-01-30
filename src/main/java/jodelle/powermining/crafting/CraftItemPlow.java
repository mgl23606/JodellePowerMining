package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the crafting logic for plow tools in the PowerMining plugin.
 *
 * <p>
 * This class extends {@link CraftItem} and is responsible for initializing
 * and modifying the metadata of plow tools using predefined crafting
 * recipes from {@link Reference#PLOW_CRAFTING_RECIPES}.
 * </p>
 */
public class CraftItemPlow extends CraftItem {

    /**
     * Constructs a {@code CraftItemPlow} and initializes crafting recipes
     * for all defined plows.
     *
     * <p>
     * This constructor iterates through the predefined plow crafting recipes,
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
     * @throws NullPointerException if a recipe key in {@link Reference#PLOW_CRAFTING_RECIPES} is null
     */
    public CraftItemPlow(@Nonnull final PowerMining plugin) {
        super(plugin);

        for (Map.Entry<String, ItemStack[]> tool : Reference.PLOW_CRAFTING_RECIPES.entrySet()) {

            // Make nullness explicit for the compiler/null analysis
            final String key = Objects.requireNonNull(tool.getKey(), "Plow recipe key is null");

            final int i = Reference.PLOWS.indexOf(key);
            final Material hoe = Reference.HOES.get(i);

            final ItemStack powerTool = new ItemStack(hoe, 1);

            modifyItemMeta(powerTool, key);
        }
    }
}
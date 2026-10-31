/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.lib;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class Reference {

    private Reference() {
        // Prevent instantiation since this is a static reference/utility class.
    }

    // List of required plugin dependencies checked on startup
    public static final String[] DEPENDENCIES = {"WorldGuard"};

    // Maps keeping track of dynamic permissions per item material type
    public static final Map<Material, String> CRAFT_PERMISSIONS = new HashMap<>();
    public static final Map<Material, String> USE_PERMISSIONS = new HashMap<>();
    public static final Map<Material, String> ENCHANT_PERMISSIONS = new HashMap<>();

    // Cached grid recipes for custom power tools loaded from recipes.json
    public static final Map<String, ItemStack[]> HAMMER_CRAFTING_RECIPES = new HashMap<>();
    public static final Map<String, ItemStack[]> EXCAVATOR_CRAFTING_RECIPES = new HashMap<>();
    public static final Map<String, ItemStack[]> PLOW_CRAFTING_RECIPES = new HashMap<>();

    // Block mining definitions loaded from mineable.json and diggable.json
    public static final Map<Material, List<Material>> MINABLE = new HashMap<>();
    public static final List<Material> DIGGABLE = new ArrayList<>();

    // Blocks that can be tilled into farmland by hoes/plows
    public static final List<Material> TILLABLE = Arrays.asList(
            Material.GRASS_BLOCK,
            Material.DIRT
    );

    // Blocks that can be flattened into paths
    public static final List<Material> PATHABLE = Collections.singletonList(
            Material.GRASS_BLOCK
    );

    // Standard vanilla pickaxe tiers (fully updated to support 26.3 copper tools)
    public static final List<Material> PICKAXES = Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.COPPER_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
    );

    // Standard vanilla shovel tiers
    public static final List<Material> SHOVELS = Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.COPPER_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
    );

    // Alias list for compatibility references
    public static final List<Material> SPADES = new ArrayList<>(SHOVELS);

    // Standard vanilla hoe tiers
    public static final List<Material> HOES = Arrays.asList(
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.COPPER_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE
    );

    // Global mining boundaries parsed from config.json
    public static Integer RADIUS;
    public static Integer DEEP;

    // String identifiers for custom power tools
    public static final List<String> HAMMERS = Arrays.asList(
            "WOODEN_HAMMER",
            "STONE_HAMMER",
            "IRON_HAMMER",
            "GOLDEN_HAMMER",
            "COPPER_HAMMER",
            "DIAMOND_HAMMER",
            "NETHERITE_HAMMER"
    );

    public static final List<String> EXCAVATORS = Arrays.asList(
            "WOODEN_EXCAVATOR",
            "STONE_EXCAVATOR",
            "IRON_EXCAVATOR",
            "GOLDEN_EXCAVATOR",
            "COPPER_EXCAVATOR",
            "DIAMOND_EXCAVATOR",
            "NETHERITE_EXCAVATOR"
    );

    public static final List<String> PLOWS = Arrays.asList(
            "WOODEN_PLOW",
            "STONE_PLOW",
            "IRON_PLOW",
            "GOLDEN_PLOW",
            "COPPER_PLOW",
            "DIAMOND_PLOW",
            "NETHERITE_PLOW"
    );
}
/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Holds constants and static references shared across the PowerMining plugin.
 */

package jodelle.powermining.lib;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class Reference {

    private Reference() {
        // Prevent instantiation — this is a static utility class.
    }

    public static final String[] DEPENDENCIES = {"WorldGuard"};

    public static final Map<Material, String> CRAFT_PERMISSIONS = new HashMap<>();
    public static final Map<Material, String> USE_PERMISSIONS = new HashMap<>();
    public static final Map<Material, String> ENCHANT_PERMISSIONS = new HashMap<>();

    public static final Map<String, ItemStack[]> HAMMER_CRAFTING_RECIPES = new HashMap<>();
    public static final Map<String, ItemStack[]> EXCAVATOR_CRAFTING_RECIPES = new HashMap<>();
    public static final Map<String, ItemStack[]> PLOW_CRAFTING_RECIPES = new HashMap<>();

    public static final Map<Material, List<Material>> MINABLE = new HashMap<>();

    public static final List<Material> DIGGABLE = new ArrayList<>();

    public static final List<Material> TILLABLE = Arrays.asList(
            Material.GRASS_BLOCK,
            Material.DIRT
    );

    public static final List<Material> PATHABLE = Collections.singletonList(
            Material.GRASS_BLOCK
    );

    public static final List<Material> PICKAXES = Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.COPPER_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
    );

    public static final List<Material> SHOVELS = Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.COPPER_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
    );

    public static final List<Material> SPADES = new ArrayList<>(SHOVELS);

    public static final List<Material> HOES = Arrays.asList(
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.COPPER_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE
    );

    public static Integer RADIUS;
    public static Integer DEEP;

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

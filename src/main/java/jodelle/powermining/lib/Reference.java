package jodelle.powermining.lib;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Stores constant references and predefined data used in the PowerMining
 * plugin.
 * 
 * <p>
 * This class contains crafting recipes, tool lists, permissions, and various
 * configuration values for tools like hammers, excavators, and plows. It also
 * manages the initialization of crafting recipes from configuration files.
 * </p>
 */
public class Reference {

    /**
     * List of plugin dependencies required for integration.
     */
    public static final String[] dependencies = { "WorldGuard", "Jobs" };

    /**
     * Maps materials to their respective crafting permissions.
     */
    public static HashMap<Material, String> CRAFT_PERMISSIONS = new HashMap<>();

    /**
     * Maps materials to their respective usage permissions.
     */
    public static HashMap<Material, String> USE_PERMISSIONS = new HashMap<>();

    /**
     * Maps materials to their respective enchantment permissions.
     */
    public static HashMap<Material, String> ENCHANT_PERMISSIONS = new HashMap<>();

    /**
     * General map containing all crafting recipes.
     */
    public static HashMap<String, ItemStack[]> CRAFTING_RECIPES = new HashMap<>();

    /**
     * Specific crafting recipes for hammers.
     */
    public static HashMap<String, ItemStack[]> HAMMER_CRAFTING_RECIPES = new HashMap<>();

    /**
     * Specific crafting recipes for excavators.
     */
    public static HashMap<String, ItemStack[]> EXCAVATOR_CRAFTING_RECIPES = new HashMap<>();

    /**
     * Specific crafting recipes for plows.
     */
    public static HashMap<String, ItemStack[]> PLOW_CRAFTING_RECIPES = new HashMap<>();

    /**
     * Defines which materials are minable with specific tools.
     */
    public static HashMap<Material, ArrayList<Material>> MINABLE = new HashMap<>();

    /**
     * List of materials that can be dug.
     */
    public static ArrayList<Material> DIGGABLE = new ArrayList<>();

    /**
     * List of materials that can be tilled using hoes.
     */
    public static ArrayList<Material> TILLABLE = new ArrayList<>(Arrays.asList(
            Material.GRASS_BLOCK,
            Material.DIRT));

    /**
     * List of materials that can be converted into paths.
     */
    public static ArrayList<Material> PATHABLE = new ArrayList<>(Collections.singletonList(
            Material.GRASS_BLOCK));

    /**
     * List of pickaxe materials used in hammer crafting.
     */
    public static ArrayList<Material> PICKAXES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE));

    /**
     * List of shovel materials used in excavator crafting.
     */
    public static ArrayList<Material> SHOVELS = new ArrayList<>(Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL));

    /**
     * List of spade materials (alias for shovels).
     */
    public static ArrayList<Material> SPADES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL));

    /**
     * List of hoe materials used in plow crafting.
     */
    public static ArrayList<Material> HOES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE));

    /**
     * Default tool radius for special mining effects.
     */
    public static Integer RADIUS = 2;

    /**
     * Default tool depth for special mining effects.
     */
    public static Integer DEPTH = 1;

    /**
     * List of hammer tool names.
     */
    public static ArrayList<String> HAMMERS = new ArrayList<>(Arrays.asList(
            "WOODEN_HAMMER", "STONE_HAMMER", "IRON_HAMMER", "GOLDEN_HAMMER", "DIAMOND_HAMMER",
            "NETHERITE_HAMMER"));

    /**
     * List of excavator tool names.
     */
    public static ArrayList<String> EXCAVATORS = new ArrayList<>(Arrays.asList(
            "WOODEN_EXCAVATOR", "STONE_EXCAVATOR", "IRON_EXCAVATOR", "GOLDEN_EXCAVATOR",
            "DIAMOND_EXCAVATOR", "NETHERITE_EXCAVATOR"));

    /**
     * List of plow tool names.
     */
    public static ArrayList<String> PLOWS = new ArrayList<>(Arrays.asList(
            "WOODEN_PLOW", "STONE_PLOW", "IRON_PLOW", "GOLDEN_PLOW", "DIAMOND_PLOW", "NETHERITE_PLOW"));

    /**
     * Initializes crafting recipes from configuration and combines them into
     * the general crafting recipe map.
     */
    static {
        // Add missing recipes from config.yml
        addRecipesFromConfig();

        // Combine all recipes into the general crafting map
        CRAFTING_RECIPES.putAll(HAMMER_CRAFTING_RECIPES);
        CRAFTING_RECIPES.putAll(EXCAVATOR_CRAFTING_RECIPES);
        CRAFTING_RECIPES.putAll(PLOW_CRAFTING_RECIPES);
    }

    /**
     * Populates the crafting recipe maps for hammers, excavators, and plows
     * by creating recipe patterns.
     */
    private static void addRecipesFromConfig() {
        // Hammer recipes
        HAMMERS.forEach(hammer -> {
            String pickaxeType = hammer.replace("_HAMMER", "_PICKAXE");
            HAMMER_CRAFTING_RECIPES.put(hammer, createRecipe("EMPTY", hammer, "EMPTY",
                    hammer, pickaxeType, hammer,
                    "EMPTY", hammer, "EMPTY"));
        });

        // Excavator recipes
        EXCAVATORS.forEach(excavator -> {
            String shovelType = excavator.replace("_EXCAVATOR", "_SHOVEL");
            EXCAVATOR_CRAFTING_RECIPES.put(excavator, createRecipe("EMPTY", excavator, "EMPTY",
                    excavator, shovelType, excavator,
                    "EMPTY", excavator, "EMPTY"));
        });

        // Plow recipes
        PLOWS.forEach(plow -> {
            String hoeType = plow.replace("_PLOW", "_HOE");
            PLOW_CRAFTING_RECIPES.put(plow, createRecipe("EMPTY", plow, "EMPTY",
                    plow, hoeType, plow,
                    "EMPTY", plow, "EMPTY"));
        });
    }

    /**
     * Creates a crafting recipe pattern using an array of material names.
     * 
     * @param materials An array representing the 3x3 crafting grid, where "EMPTY"
     *                  represents an empty slot.
     * @return An {@link ItemStack} array representing the crafting recipe.
     */
    private static ItemStack[] createRecipe(String... materials) {
        ItemStack[] recipe = new ItemStack[9];
        for (int i = 0; i < materials.length; i++) {
            Material mat = materials[i].equals("EMPTY") ? Material.AIR : Material.getMaterial(materials[i]);
            recipe[i] = (mat != null) ? new ItemStack(mat) : new ItemStack(Material.AIR);
        }
        return recipe;
    }
}
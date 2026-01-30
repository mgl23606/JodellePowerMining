package jodelle.powermining.lib;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import javax.annotation.Nonnull;

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
     * Prevents instantiation of this utility class.
     */
    private Reference() {
        throw new UnsupportedOperationException("Utility class");
    }

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
    public static final @Nonnull ArrayList<Material> PICKAXES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE));

    /**
     * List of shovel materials used in excavator crafting.
     */
    public static final @Nonnull ArrayList<Material> SHOVELS = new ArrayList<>(Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL));

    /**
     * List of spade materials (alias for shovels).
     */
    public static final @Nonnull ArrayList<Material> SPADES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL));

    /**
     * List of hoe materials used in plow crafting.
     */
    public static final @Nonnull ArrayList<Material> HOES = new ArrayList<>(Arrays.asList(
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
    public static final @Nonnull ArrayList<String> HAMMERS = new ArrayList<>(Arrays.asList(
            "WOODEN_HAMMER", "STONE_HAMMER", "IRON_HAMMER", "GOLDEN_HAMMER", "DIAMOND_HAMMER",
            "NETHERITE_HAMMER"));

    /**
     * List of excavator tool names.
     */
    public static final @Nonnull ArrayList<String> EXCAVATORS = new ArrayList<>(Arrays.asList(
            "WOODEN_EXCAVATOR", "STONE_EXCAVATOR", "IRON_EXCAVATOR", "GOLDEN_EXCAVATOR",
            "DIAMOND_EXCAVATOR", "NETHERITE_EXCAVATOR"));

    /**
     * List of plow tool names.
     */
    public static final @Nonnull ArrayList<String> PLOWS = new ArrayList<>(Arrays.asList(
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
     * Resolves the base crafting ingredient for a PowerTool based on its tier.
     *
     * <p>
     * This method derives the primary material used in crafting recipes (e.g. the
     * "head" material) from the tool's name prefix. The mapping follows the
     * convention used by the plugin's default configuration:
     * </p>
     *
     * <ul>
     * <li>{@code WOODEN_*} → {@code OAK_LOG}</li>
     * <li>{@code STONE_*} → {@code STONE}</li>
     * <li>{@code IRON_*} → {@code IRON_INGOT}</li>
     * <li>{@code GOLDEN_*} → {@code GOLD_INGOT}</li>
     * <li>{@code DIAMOND_*} → {@code DIAMOND}</li>
     * <li>{@code NETHERITE_*}→ {@code NETHERITE_INGOT}</li>
     * </ul>
     *
     * <p>
     * The returned value is the {@link org.bukkit.Material} name as a
     * {@link String}
     * and is intended to be passed into recipe construction utilities such as
     * {@code createRecipe(...)}.
     * </p>
     *
     * @param toolName
     *                 The full tool identifier (e.g. {@code DIAMOND_EXCAVATOR}).
     *                 Must not be {@code null}.
     *
     * @return
     *         The material name representing the crafting ingredient for the
     *         tool's tier.
     *
     * @throws IllegalArgumentException
     *                                  If the tool name does not start with a
     *                                  recognized tier prefix.
     */
    private static @Nonnull String getTierIngredient(final @Nonnull String toolName) {
        // English comments as requested
        if (toolName.startsWith("WOODEN_")) {
            return "OAK_LOG";
        }
        if (toolName.startsWith("STONE_")) {
            return "STONE";
        }
        if (toolName.startsWith("IRON_")) {
            return "IRON_INGOT";
        }
        if (toolName.startsWith("GOLDEN_")) {
            return "GOLD_INGOT";
        }
        if (toolName.startsWith("DIAMOND_")) {
            return "DIAMOND";
        }
        if (toolName.startsWith("NETHERITE_")) {
            return "NETHERITE_INGOT";
        }
        // English comments as requested
        throw new IllegalArgumentException("Unknown tool tier for: " + toolName);
    }

    /**
     * Initializes the default crafting recipes for all PowerTool categories.
     *
     * <p>
     * This method populates the internal recipe maps for:
     * </p>
     * <ul>
     * <li>Hammers</li>
     * <li>Excavators</li>
     * <li>Plows</li>
     * </ul>
     *
     * <p>
     * For each tool type, a fixed 3x3 recipe layout is generated using:
     * </p>
     * <ul>
     * <li>The tier-based ingredient resolved via
     * {@link #getTierIngredient(String)}</li>
     * <li>The corresponding vanilla tool (pickaxe, shovel, or hoe) placed
     * in the center slot</li>
     * <li>{@code EMPTY} slots represented explicitly to preserve shape</li>
     * </ul>
     *
     * <p>
     * The resulting {@link ItemStack} arrays are stored in their respective
     * crafting maps ({@code HAMMER_CRAFTING_RECIPES},
     * {@code EXCAVATOR_CRAFTING_RECIPES}, {@code PLOW_CRAFTING_RECIPES}) and are
     * later used both for recipe registration and for crafting validation.
     * </p>
     *
     * <p>
     * This method is intended to be invoked once during static initialization.
     * </p>
     */
    private static void addRecipesFromConfig() {
        // Hammer recipes
        HAMMERS.forEach(hammerName -> {
            final @Nonnull String hammer = Objects.requireNonNull(hammerName, "hammer");
            final String headMaterial = getTierIngredient(hammer);
            final String pickaxeType = hammer.replace("_HAMMER", "_PICKAXE");

            HAMMER_CRAFTING_RECIPES.put(hammer, createRecipe(
                    "EMPTY", headMaterial, "EMPTY",
                    headMaterial, pickaxeType, headMaterial,
                    "EMPTY", headMaterial, "EMPTY"));
        });

        // Excavator recipes
        EXCAVATORS.forEach(excavatorName -> {
            final @Nonnull String excavator = Objects.requireNonNull(excavatorName, "excavator");
            final String headMaterial = getTierIngredient(excavator);
            final String shovelType = excavator.replace("_EXCAVATOR", "_SHOVEL");

            EXCAVATOR_CRAFTING_RECIPES.put(excavator, createRecipe(
                    "EMPTY", headMaterial, "EMPTY",
                    headMaterial, shovelType, headMaterial,
                    "EMPTY", headMaterial, "EMPTY"));
        });

        // Plow recipes
        PLOWS.forEach(plowName -> {
            final @Nonnull String plow = Objects.requireNonNull(plowName, "plow");
            final String headMaterial = getTierIngredient(plow);
            final String hoeType = plow.replace("_PLOW", "_HOE");

            PLOW_CRAFTING_RECIPES.put(plow, createRecipe(
                    "EMPTY", headMaterial, "EMPTY",
                    headMaterial, hoeType, headMaterial,
                    "EMPTY", headMaterial, "EMPTY"));
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
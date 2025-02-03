package jodelle.powermining.lib;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Reference {

    public static final String[] dependencies = {"WorldGuard"};

    public static HashMap<Material, String> CRAFT_PERMISSIONS = new HashMap<>();
    public static HashMap<Material, String> USE_PERMISSIONS = new HashMap<>();
    public static HashMap<Material, String> ENCHANT_PERMISSIONS = new HashMap<>();

    public static HashMap<String, ItemStack[]> CRAFTING_RECIPES = new HashMap<>();
    public static HashMap<String, ItemStack[]> HAMMER_CRAFTING_RECIPES = new HashMap<>();
    public static HashMap<String, ItemStack[]> EXCAVATOR_CRAFTING_RECIPES = new HashMap<>();
    public static HashMap<String, ItemStack[]> PLOW_CRAFTING_RECIPES = new HashMap<>();

    public static HashMap<Material, ArrayList<Material>> MINABLE = new HashMap<>();
    public static ArrayList<Material> DIGGABLE = new ArrayList<>();
    public static ArrayList<Material> TILLABLE = new ArrayList<>(Arrays.asList(
            Material.GRASS_BLOCK,
            Material.DIRT
    ));

    public static ArrayList<Material> PATHABLE = new ArrayList<>(Collections.singletonList(
            Material.GRASS_BLOCK
    ));

    public static ArrayList<Material> PICKAXES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
    ));

    public static ArrayList<Material> SHOVELS = new ArrayList<>(Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
    ));

    public static ArrayList<Material> SPADES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
    ));

    public static ArrayList<Material> HOES = new ArrayList<>(Arrays.asList(
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE
    ));

    public static Integer RADIUS = 2;
    public static Integer DEPTH = 1;

    public static ArrayList<String> HAMMERS = new ArrayList<>(Arrays.asList(
            "WOODEN_HAMMER", "STONE_HAMMER", "IRON_HAMMER", "GOLDEN_HAMMER", "DIAMOND_HAMMER", "NETHERITE_HAMMER"
    ));

    public static ArrayList<String> EXCAVATORS = new ArrayList<>(Arrays.asList(
            "WOODEN_EXCAVATOR", "STONE_EXCAVATOR", "IRON_EXCAVATOR", "GOLDEN_EXCAVATOR", "DIAMOND_EXCAVATOR", "NETHERITE_EXCAVATOR"
    ));

    public static ArrayList<String> PLOWS = new ArrayList<>(Arrays.asList(
            "WOODEN_PLOW", "STONE_PLOW", "IRON_PLOW", "GOLDEN_PLOW", "DIAMOND_PLOW", "NETHERITE_PLOW"
    ));

    static {
        // Add missing recipes from config.yml
        addRecipesFromConfig();
        
        // Combine all recipes into the general crafting map
        CRAFTING_RECIPES.putAll(HAMMER_CRAFTING_RECIPES);
        CRAFTING_RECIPES.putAll(EXCAVATOR_CRAFTING_RECIPES);
        CRAFTING_RECIPES.putAll(PLOW_CRAFTING_RECIPES);
    }

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

    private static ItemStack[] createRecipe(String... materials) {
        ItemStack[] recipe = new ItemStack[9];
        for (int i = 0; i < materials.length; i++) {
            Material mat = materials[i].equals("EMPTY") ? Material.AIR : Material.getMaterial(materials[i]);
            recipe[i] = (mat != null) ? new ItemStack(mat) : new ItemStack(Material.AIR);
        }
        return recipe;
    }
}
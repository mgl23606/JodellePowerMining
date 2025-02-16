package jodelle.powermining.managers;

import jodelle.powermining.PowerMining;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.*;

/**
 * Handles the migration of the configuration file to ensure compatibility with newer versions of the PowerMining plugin.
 * 
 * <p>
 * This class checks for outdated configuration formats and updates them to the latest version. 
 * It ensures that required settings are present, renames deprecated keys, and converts the recipe format 
 * to a structured format while enforcing a consistent order of tools.
 * </p>
 */
public class ConfigMigrator {

    private final PowerMining plugin;

    /**
     * The current version of the configuration format.
     */
    private static final String CURRENT_VERSION = "1.0"; // New version for migration

    /**
     * Constructs a {@code ConfigMigrator} instance.
     * 
     * @param plugin The instance of {@link PowerMining} responsible for managing the configuration.
     */
    public ConfigMigrator(PowerMining plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks and migrates the configuration file if it is outdated.
     * 
     * <p>
     * This method ensures that required configuration keys exist, renames deprecated keys, 
     * removes old sections, and converts recipes into the new structured format while maintaining 
     * a predefined order for tools.
     * </p>
     */
    public void migrateConfig() {
        FileConfiguration config = plugin.getConfig();
        String configVersion = config.getString("configVersion", null);

        // If config is already updated, stop migration
        if ("1.0".equals(configVersion)) {
            plugin.getLogger().info("Config is already up-to-date (v1.0). No migration needed.");
            return;
        }

        plugin.getLogger().info("Migrating config to version " + CURRENT_VERSION + "...");

        // Add missing keys if not present
        if (!config.contains("language")) {
            config.set("language", "en_US");
            plugin.getLogger().info("Added missing config key: language (default: en_US)");
        }

        if (!config.contains("debug")) {
            config.set("debug", false);
            plugin.getLogger().info("Added missing config key: debug (default: false)");
        }

        // Rename 'Deep' to 'Depth' if 'Deep' exists
        if (config.contains("Deep")) {
            int depthValue = config.getInt("Deep", 0);
            config.set("Depth", depthValue);
            config.set("Deep", null);
            plugin.getLogger().info("Renamed 'Deep' to 'Depth' (Value: " + depthValue + ")");
        }

        // STEP 1: Extract Old Recipes Before Deleting
        List<?> oldRecipes = config.getList("Recipes"); // Store before deleting

        // STEP 2: Remove Old "Recipes" Section
        if (config.contains("Recipes")) {
            config.set("Recipes", null);
            plugin.getLogger().info("Removed old 'Recipes' section.");
        }

        // STEP 3: Convert Old Recipe Format to New Format
        Map<String, Map<String, Object>> newRecipes = new HashMap<>();

        if (oldRecipes != null) {
            for (Object entry : oldRecipes) {
                if (!(entry instanceof LinkedHashMap))
                    continue;
                LinkedHashMap<?, ?> rawMap = (LinkedHashMap<?, ?>) entry;

                for (Map.Entry<?, ?> rawEntry : rawMap.entrySet()) {
                    if (!(rawEntry.getKey() instanceof String) || !(rawEntry.getValue() instanceof List)) {
                        plugin.getLogger().warning("Skipping invalid recipe format: " + rawEntry);
                        continue;
                    }

                    String toolName = (String) rawEntry.getKey();
                    List<?> rawList = (List<?>) rawEntry.getValue();
                    Map<String, String> ingredients = new LinkedHashMap<>();

                    String[] layout = { "", "", "" };
                    for (int i = 0; i < rawList.size(); i++) {
                        String value = (String) rawList.get(i);
                        int row = i / 3;
                        char symbol = (char) ('A' + i);

                        if (value.equalsIgnoreCase("EMPTY") || value.equalsIgnoreCase("AIR")) {
                            ingredients.put(String.valueOf(symbol), "AIR");
                        } else {
                            ingredients.put(String.valueOf(symbol), value);
                        }

                        layout[row] += symbol;
                    }

                    Map<String, Object> orderedRecipeData = new LinkedHashMap<>();
                    orderedRecipeData.put("recipe-ingredients", ingredients);
                    orderedRecipeData.put("recipe-shape", Arrays.asList(layout));

                    newRecipes.put(toolName, orderedRecipeData);
                }
            }
        }

        // STEP 4: Enforce Tool Order
        List<String> toolOrder = Arrays.asList(
                "WOODEN_HAMMER", "STONE_HAMMER", "IRON_HAMMER", "GOLDEN_HAMMER", "DIAMOND_HAMMER", "NETHERITE_HAMMER",
                "WOODEN_EXCAVATOR", "STONE_EXCAVATOR", "IRON_EXCAVATOR", "GOLDEN_EXCAVATOR", "DIAMOND_EXCAVATOR",
                "NETHERITE_EXCAVATOR",
                "WOODEN_PLOW", "STONE_PLOW", "IRON_PLOW", "GOLDEN_PLOW", "DIAMOND_PLOW", "NETHERITE_PLOW");

        Map<String, Map<String, Object>> migratedRecipes = new LinkedHashMap<>();

        for (String toolName : toolOrder) {
            if (!newRecipes.containsKey(toolName))
                continue;

            Map<String, Object> recipeData = newRecipes.get(toolName);

            Map<String, Object> orderedRecipeData = new LinkedHashMap<>();
            if (recipeData.containsKey("recipe-ingredients")) {
                orderedRecipeData.put("recipe-ingredients", recipeData.get("recipe-ingredients"));
            }
            if (recipeData.containsKey("recipe-shape")) {
                orderedRecipeData.put("recipe-shape", recipeData.get("recipe-shape"));
            }

            migratedRecipes.put(toolName, orderedRecipeData);
        }

        // STEP 5: Store the Correctly Ordered Recipes in "recipes"
        config.set("recipes", migratedRecipes);
        plugin.getLogger().info("Recipes migrated successfully with enforced tool order.");

        // STEP 6: Save Config (Now That Everything is Set)
        config.set("configVersion", CURRENT_VERSION);
        plugin.getLogger().info("Set configVersion to " + CURRENT_VERSION);
        plugin.saveConfig();
        plugin.reloadConfig(); // Reload config, so recipes are correctly loaded
        plugin.getLogger().info("Config migration complete.");
    }
}
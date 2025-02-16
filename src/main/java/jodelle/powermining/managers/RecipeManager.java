package jodelle.powermining.managers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.crafting.CraftItem;
import jodelle.powermining.crafting.CraftItemExcavator;
import jodelle.powermining.crafting.CraftItemHammer;
import jodelle.powermining.crafting.CraftItemPlow;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;

import java.util.*;

/**
 * Manages the registration and unregistration of custom crafting recipes for
 * the PowerMining plugin.
 * 
 * <p>
 * This class is responsible for loading recipes from the configuration file,
 * registering them in the game, handling fallback recipes when necessary, and
 * removing recipes when they are no longer needed.
 * </p>
 */
public class RecipeManager {
    private final PowerMining plugin;
    private final List<NamespacedKey> registeredRecipes = new ArrayList<>();
    private final DebuggingMessages debuggingMessages;

    /**
     * Constructs a {@code RecipeManager} instance.
     * 
     * @param plugin The instance of {@link PowerMining} responsible for managing
     *               recipes.
     */
    public RecipeManager(PowerMining plugin) {
        debuggingMessages = plugin.getDebuggingMessages();
        this.plugin = plugin;
    }

    /**
     * Loads and registers crafting recipes from the configuration file.
     * 
     * <p>
     * If no valid recipes are found in the configuration, the manager falls back
     * to the default recipes defined in {@link Reference}.
     * </p>
     */
    public void registerRecipes() {
        plugin.getLogger().info("Loading custom crafting recipes...");

        FileConfiguration config = plugin.getConfig();

        if (config.contains("recipes")) {
            loadRecipesFromConfig(config);
        }

        if (registeredRecipes.isEmpty()) {
            plugin.getLogger()
                    .warning("No valid recipes found in config.yml, falling back to default Reference recipes.");
            loadDefaultRecipes();
        }
    }

    /**
     * Loads crafting recipes from the configuration file and registers them.
     * 
     * @param config The {@link FileConfiguration} containing recipe data.
     */
    private void loadRecipesFromConfig(FileConfiguration config) {
        plugin.getLogger().info("Loading recipes from config.yml...");

        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection == null) {
            plugin.getLogger().warning("No recipes section found in config.yml.");
            return;
        }

        for (String key : recipesSection.getKeys(false)) {
            NamespacedKey recipeKey = new NamespacedKey(plugin, key);
            if (registeredRecipes.contains(recipeKey)) {
                debuggingMessages.sendConsoleMessage("Skipping duplicate recipe: " + key);
                continue;
            }

            Map<Character, ItemStack> ingredientsMap = parseIngredients(config, key);
            List<String> shape = config.getStringList("recipes." + key + ".recipe-shape");
            shape.replaceAll(String::trim);

            if (shape.size() != 3 || shape.stream().anyMatch(row -> row.length() != 3)) {
                plugin.getLogger().warning("Skipping recipe for " + key + " - Invalid shape size.");
                continue;
            }

            ShapedRecipe recipe = createShapedRecipe(recipeKey, key, ingredientsMap, shape);
            if (recipe != null) {
                Bukkit.addRecipe(recipe);
                registeredRecipes.add(recipeKey);
                debuggingMessages.sendConsoleMessage("Successfully registered recipe: " + key);
            }
        }
    }

    /**
     * Parses the ingredient list from the configuration file.
     * 
     * @param config The {@link FileConfiguration} containing recipe data.
     * @param key    The name of the recipe being parsed.
     * @return A map of character keys to corresponding {@link ItemStack}
     *         ingredients.
     */
    private Map<Character, ItemStack> parseIngredients(FileConfiguration config, String key) {
        Map<Character, ItemStack> ingredientsMap = new HashMap<>();
        ConfigurationSection ingredientsSection = config
                .getConfigurationSection("recipes." + key + ".recipe-ingredients");

        if (ingredientsSection == null) {
            return ingredientsMap;
        }

        for (String ingredientKey : ingredientsSection.getKeys(false)) {
            String value = ingredientsSection.getString(ingredientKey);

            // Skip invalid or empty materials early
            if (value == null || value.equalsIgnoreCase("EMPTY") || value.equalsIgnoreCase("AIR")) {
                continue;
            }

            String[] parts = value.split("\\*");
            Material material = Material.getMaterial(parts[0]);
            int amount = (parts.length > 1) ? Integer.parseInt(parts[1]) : 1;

            if (material != null) {
                ingredientsMap.put(ingredientKey.charAt(0), new ItemStack(material, amount));
            } else {
                plugin.getLogger().warning("Invalid material in recipe: " + value + " for key: " + key);
            }
        }
        return ingredientsMap;
    }

    /**
     * Creates a {@link ShapedRecipe} from the parsed ingredients and shape.
     * 
     * @param key            The {@link NamespacedKey} for the recipe.
     * @param toolName       The name of the tool being crafted.
     * @param ingredientsMap The map of ingredient characters to their corresponding
     *                       {@link ItemStack}.
     * @param shape          The crafting shape defined in the config.
     * @return The created {@link ShapedRecipe}, or {@code null} if invalid.
     */
    private ShapedRecipe createShapedRecipe(NamespacedKey key, String toolName,
            Map<Character, ItemStack> ingredientsMap, List<String> shape) {

        ItemStack result = getToolItem(toolName);
        if (result == null) {
            plugin.getLogger().warning("Skipping recipe: Unknown tool " + toolName);
            return null;
        }

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape.get(0), shape.get(1), shape.get(2));

        // Set ingredients only if they are not AIR
        for (Map.Entry<Character, ItemStack> entry : ingredientsMap.entrySet()) {
            Material material = entry.getValue().getType();
            if (material != Material.AIR) {
                recipe.setIngredient(entry.getKey(), new RecipeChoice.ExactChoice(entry.getValue()));
            } else {
                plugin.getLogger().warning("Skipping AIR material for ingredient key: " + entry.getKey());
            }
        }

        return recipe;
    }

    /**
     * Loads and registers fallback recipes from {@link Reference} when no custom
     * recipes are found.
     */
    private void loadDefaultRecipes() {
        new CraftItemHammer(plugin);
        new CraftItemExcavator(plugin);
        new CraftItemPlow(plugin);

        registerFallbackRecipes(Reference.HAMMERS, Reference.HAMMER_CRAFTING_RECIPES);
        registerFallbackRecipes(Reference.EXCAVATORS, Reference.EXCAVATOR_CRAFTING_RECIPES);
        registerFallbackRecipes(Reference.PLOWS, Reference.PLOW_CRAFTING_RECIPES);
    }

    /**
     * Registers fallback recipes for PowerTools when no custom recipes are found in
     * the configuration.
     * 
     * <p>
     * This method iterates through a list of tools and their corresponding crafting
     * recipes,
     * ensuring that they are properly registered in the game. If a recipe is
     * already registered
     * or invalid, it is skipped. Each tool is assigned a 3x3 shaped recipe,
     * ensuring consistency
     * with the crafting system.
     * </p>
     * 
     * @param tools           A {@link List} of tool names to be registered.
     * @param craftingRecipes A {@link HashMap} mapping tool names to their
     *                        corresponding {@link ItemStack[]} recipes.
     */

    private void registerFallbackRecipes(List<String> tools, HashMap<String, ItemStack[]> craftingRecipes) {
        for (String toolName : tools) {
            NamespacedKey key = new NamespacedKey(plugin, toolName);

            // Check if recipe is already registered before adding
            if (registeredRecipes.contains(key) || Bukkit.getRecipe(key) != null) {
                debuggingMessages.sendConsoleMessage("Skipping duplicate fallback recipe: " + toolName);
                continue;
            }

            ItemStack resultItem = getToolItem(toolName);

            if (resultItem == null) {
                debuggingMessages.sendConsoleMessage("Skipping recipe registration: Tool " + toolName + " is null!");
                continue;
            }

            if (craftingRecipes.containsKey(toolName)) {
                ItemStack[] recipeItems = craftingRecipes.get(toolName);

                if (recipeItems == null || recipeItems.length != 9) {
                    plugin.getLogger().warning("Invalid recipe data for " + toolName);
                    continue;
                }

                ShapedRecipe recipe = new ShapedRecipe(key, resultItem);
                recipe.shape("ABC", "DEF", "GHI");
                char[] slots = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' };

                for (int i = 0; i < slots.length; i++) {
                    if (recipeItems[i] != null && recipeItems[i].getType() != Material.AIR) {
                        recipe.setIngredient(slots[i], recipeItems[i].getType());
                    }
                }

                Bukkit.addRecipe(recipe);
                registeredRecipes.add(key);
                debuggingMessages.sendConsoleMessage("Registered fallback recipe: " + toolName);
            }
        }
    }

    /**
     * Unregisters all custom crafting recipes from the game.
     */
    public void unregisterRecipes() {
        plugin.getLogger().info("Unloading custom crafting recipes...");

        // Scan through all recipes and remove matching ones
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        List<NamespacedKey> toRemove = new ArrayList<>();

        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe instanceof Keyed) {
                NamespacedKey key = ((Keyed) recipe).getKey();
                if (registeredRecipes.contains(key)) {
                    toRemove.add(key);
                }
            }
        }

        // Remove them from Bukkit
        for (NamespacedKey key : toRemove) {
            Bukkit.removeRecipe(key);
            debuggingMessages.sendConsoleMessage("Removed recipe: " + key.getKey());
        }

        registeredRecipes.clear();
        plugin.getLogger().info("All custom recipes unloaded.");
    }

    /**
     * Retrieves the corresponding {@link ItemStack} for a given tool name.
     * 
     * @param toolName The name of the tool being crafted.
     * @return The crafted {@link ItemStack}, or a default placeholder if not found.
     */
    private ItemStack getToolItem(String toolName) {
        // 🔹 First, check if the tool exists in Reference.CRAFTING_RECIPES
        if (Reference.CRAFTING_RECIPES.containsKey(toolName)) {
            ItemStack[] recipe = Reference.CRAFTING_RECIPES.get(toolName);
            if (recipe != null && recipe.length > 0 && recipe[4] != null) { // Ensure a valid center item exists
                ItemStack craftedItem = recipe[4];

                // Apply metadata using the appropriate CraftItem subclass
                return applyCraftItemMeta(toolName, craftedItem);
            }
        }

        // 🔹 Second, check if the tool is a valid Bukkit Material
        Material material = Material.getMaterial(toolName);
        if (material != null) {
            ItemStack craftedItem = new ItemStack(material);

            // Apply metadata using the appropriate CraftItem subclass
            return applyCraftItemMeta(toolName, craftedItem);
        }

        // 🔹 If nothing is found, log a warning and return a placeholder
        plugin.getLogger().warning("Warning: Could not find tool item for " + toolName + ". Using default.");
        return new ItemStack(Material.WOODEN_PICKAXE); // Prevents crashes by providing a default
    }

    /**
     * Applies metadata to a crafted tool item.
     * 
     * @param toolName The name of the tool.
     * @param toolItem The {@link ItemStack} to modify.
     * @return The modified {@link ItemStack}.
     */
    private ItemStack applyCraftItemMeta(String toolName, ItemStack toolItem) {
        if (toolItem == null || toolItem.getType() == Material.AIR) {
            return toolItem;
        }

        // Use the base CraftItem class to apply metadata
        CraftItem craftItem = new CraftItem(plugin);
        craftItem.modifyItemMeta(toolItem, toolName);

        return toolItem;
    }
}

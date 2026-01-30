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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    private final @Nonnull PowerMining plugin;
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
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
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
     * <p>
     * This method reads recipe definitions from the {@code recipes} section of the
     * given
     * configuration, validates the recipe shape, and registers each valid
     * {@link ShapedRecipe}
     * via {@link Bukkit#addRecipe(Recipe)}. Duplicate recipe keys are skipped.
     * </p>
     *
     * <p>
     * If the {@code recipes} section is missing, nothing is registered and a
     * warning is logged.
     * Invalid shapes (not exactly 3 rows or rows not exactly 3 characters long) are
     * skipped.
     * If a recipe result item cannot be resolved (unknown tool name), the recipe is
     * skipped.
     * </p>
     *
     * @param config The {@link FileConfiguration} containing recipe data. Must not
     *               be {@code null}.
     */
    private void loadRecipesFromConfig(FileConfiguration config) {
        plugin.getLogger().info("Loading recipes from config.yml...");

        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection == null) {
            plugin.getLogger().warning("No recipes section found in config.yml.");
            return;
        }

        for (String key : recipesSection.getKeys(false)) {
            final @Nonnull String toolName = Objects.requireNonNull(key, "recipe key");

            final NamespacedKey recipeKey = new NamespacedKey(plugin, toolName);
            if (registeredRecipes.contains(recipeKey)) {
                debuggingMessages.sendConsoleMessage("Skipping duplicate recipe: " + toolName);
                continue;
            }

            final @Nonnull Map<Character, ItemStack> ingredientsMap = Objects
                    .requireNonNull(parseIngredients(config, toolName), "ingredientsMap");

            final @Nonnull List<String> shape = Objects
                    .requireNonNull(config.getStringList("recipes." + toolName + ".recipe-shape"), "shape");
            shape.replaceAll(String::trim);

            if (shape.size() != 3 || shape.stream().anyMatch(row -> row.length() != 3)) {
                plugin.getLogger().warning("Skipping recipe for " + toolName + " - Invalid shape size.");
                continue;
            }

            final @Nullable ShapedRecipe recipe = createShapedRecipe(recipeKey, toolName, ingredientsMap, shape);
            if (recipe != null) {
                Bukkit.addRecipe(recipe);
                registeredRecipes.add(recipeKey);
                debuggingMessages.sendConsoleMessage("Successfully registered recipe: " + toolName);
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
    private @Nonnull Map<Character, ItemStack> parseIngredients(final @Nonnull FileConfiguration config,
            final @Nonnull String key) {
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
     * <p>
     * If the tool name cannot be resolved to a valid result item,
     * the recipe is skipped and {@code null} is returned.
     * </p>
     *
     * @param key            The {@link NamespacedKey} identifying the recipe.
     * @param toolName       The name of the tool being crafted. Must not be
     *                       {@code null}.
     * @param ingredientsMap The map of ingredient characters to their corresponding
     *                       {@link ItemStack} values.
     * @param shape          The crafting shape defined in the configuration.
     * @return The created {@link ShapedRecipe}, or {@code null} if the recipe
     *         is invalid or the tool name is unknown.
     */
    private @Nullable ShapedRecipe createShapedRecipe(final @Nonnull NamespacedKey key,
            final @Nonnull String toolName,
            final @Nonnull Map<Character, ItemStack> ingredientsMap,
            final @Nonnull List<String> shape) {

        final @Nullable ItemStack result = getToolItem(toolName);
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
     * Registers fallback crafting recipes for PowerTools when no custom recipes
     * are found in the configuration.
     *
     * <p>
     * This method iterates over the provided list of tool names and registers their
     * corresponding fallback crafting recipes defined in the given recipe map.
     * Recipes are skipped if they are already registered, invalid, or if the tool
     * name cannot be resolved to a valid result item.
     * </p>
     *
     * <p>
     * Tool names that are {@code null}, unknown, or mapped to invalid recipe data
     * are skipped.
     * </p>
     *
     * <p>
     * Each fallback recipe uses a fixed 3x3 shaped layout. Only non-null and
     * non-AIR ingredients are applied to the recipe.
     * </p>
     *
     * @param tools
     *                        A list of tool names to register fallback recipes for.
     *                        Entries may not be {@code null}; null entries are
     *                        skipped.
     * @param craftingRecipes
     *                        A map linking tool names to their corresponding 3x3
     *                        crafting recipes.
     *                        Each recipe must contain exactly 9 {@link ItemStack}
     *                        entries.
     */
    private void registerFallbackRecipes(
            final List<String> tools,
            final HashMap<String, ItemStack[]> craftingRecipes) {

        for (String toolName : tools) {
            final @Nonnull String safeToolName = java.util.Objects.requireNonNull(toolName, "toolName");

            final NamespacedKey key = new NamespacedKey(plugin, safeToolName);

            // Skip already registered recipes
            if (registeredRecipes.contains(key) || Bukkit.getRecipe(key) != null) {
                debuggingMessages.sendConsoleMessage("Skipping duplicate fallback recipe: " + safeToolName);
                continue;
            }

            // Resolve result item
            final @Nullable ItemStack resultItem = getToolItem(safeToolName);
            if (resultItem == null) {
                debuggingMessages.sendConsoleMessage(
                        "Skipping recipe registration: Tool " + safeToolName + " is unknown.");
                continue;
            }

            // Load recipe definition
            if (!craftingRecipes.containsKey(safeToolName)) {
                plugin.getLogger().warning("No fallback recipe defined for " + safeToolName);
                continue;
            }

            final ItemStack[] recipeItems = craftingRecipes.get(safeToolName);
            if (recipeItems == null || recipeItems.length != 9) {
                plugin.getLogger().warning("Invalid recipe data for " + safeToolName);
                continue;
            }

            // Create shaped recipe
            final ShapedRecipe recipe = new ShapedRecipe(key, resultItem);
            recipe.shape("ABC", "DEF", "GHI");

            final char[] slots = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' };

            for (int i = 0; i < slots.length; i++) {
                final ItemStack ingredient = recipeItems[i];
                if (ingredient != null && ingredient.getType() != Material.AIR) {
                    recipe.setIngredient(slots[i], ingredient.getType());
                }
            }

            Bukkit.addRecipe(recipe);
            registeredRecipes.add(key);
            debuggingMessages.sendConsoleMessage("Registered fallback recipe: " + safeToolName);
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
     * <p>
     * The method first checks custom crafting recipes defined in
     * {@link Reference#CRAFTING_RECIPES}. If no matching entry is found,
     * it attempts to resolve the tool name as a Bukkit {@link Material}.
     * </p>
     *
     * @param toolName The name of the tool being crafted. Must not be {@code null}.
     * @return The crafted {@link ItemStack}, or {@code null} if the tool name
     *         cannot be resolved to a valid item.
     */
    private @Nullable ItemStack getToolItem(final @Nonnull String toolName) {
        java.util.Objects.requireNonNull(toolName, "toolName");

        final ItemStack[] recipe = Reference.CRAFTING_RECIPES.get(toolName);
        if (recipe != null && recipe.length > 4 && recipe[4] != null) {
            final ItemStack craftedItem = recipe[4].clone();
            return applyCraftItemMeta(toolName, craftedItem);
        }

        final Material material = Material.getMaterial(toolName);
        if (material != null) {
            final ItemStack craftedItem = new ItemStack(material);
            return applyCraftItemMeta(toolName, craftedItem);
        }

        return null;
    }

    /**
     * Applies metadata to a crafted tool item.
     *
     * @param toolName The tool name to apply. Must not be {@code null}.
     * @param toolItem The {@link ItemStack} to modify. May be {@code null}.
     * @return The modified {@link ItemStack}. If {@code toolItem} is {@code null}
     *         or {@link Material#AIR},
     *         the original value is returned unchanged.
     */
    private @Nonnull ItemStack applyCraftItemMeta(final @Nonnull String toolName,
            final @Nullable ItemStack toolItem) {
        if (toolItem == null || toolItem.getType() == Material.AIR) {
            // If toolItem is null, return null is allowed because parameter is @Nullable.
            // If toolItem is AIR, we just return it unchanged.
            return toolItem == null ? new ItemStack(Material.AIR) : toolItem;
        }

        // plugin should be declared as @Nonnull in the class field to avoid nullness
        // warnings here
        final CraftItem craftItem = new CraftItem(plugin);
        craftItem.modifyItemMeta(toolItem, toolName);

        return toolItem;
    }

}

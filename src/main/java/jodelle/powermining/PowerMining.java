/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jodelle.powermining.handlers.*;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PowerMining extends JavaPlugin {

    // --- Plugin Fields ---
    public JavaPlugin plugin;
    private PlayerInteractHandler handlerPlayerInteract;
    private BlockBreakHandler handlerBlockBreak;
    private CraftItemHandler handlerCraftItem;
    private EnchantItemHandler handlerEnchantItem;
    private InventoryClickHandler handlerInventoryClick;
    private ClickPlayerHandler handlerClickPlayer;
    private CommandHandler commandHandler;
    private DebuggingMessages debuggingMessages;

    private WorldGuardPlugin worldguard;
    private static PowerMining instance;

    // --- Configuration Fields ---
    private FileConfiguration recipesConfig;
    private File recipesFile; // Note: Only used in loadRecipesConfig, could be local if desired.

    private FileConfiguration generalConfig;
    private FileConfiguration mineableConfig;
    private FileConfiguration diggableConfig;

    @Override
    public void onEnable() {
        instance = this;
        debuggingMessages = new DebuggingMessages();

        // --- 1. FILE GENERATION ---
        generateDefaultRecipesFile();
        generateDefaultConfig();
        generateDiggableFile();
        generateMineableFile();

        // --- 2. CONFIGURATION LOADING & VALIDATION ---
        // Load methods now contain validation and return false on failure.
        if (!loadRecipesConfig()) {
            getLogger().severe("FAILED to load recipes.json. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!loadConfigFile()) {
            getLogger().severe("FAILED to load config.json. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!loadMineableFile()) {
            getLogger().severe("FAILED to load mineable.json. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!loadDiggableFile()) {
            getLogger().severe("FAILED to load diggable.json. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- 3. PROCESSING AND INITIALIZATION ---

        // Process general settings, mineable, and diggable lists
        processConfig();

        // Process crafting recipes
        processCraftingRecipes();

        processPermissions();
        getLogger().info("Finished processing all config files.");
        loadDependencies();

        // --- 4. HANDLER REGISTRATION ---
        handlerPlayerInteract = new PlayerInteractHandler();
        handlerBlockBreak = new BlockBreakHandler();
        handlerCraftItem = new CraftItemHandler();
        handlerEnchantItem = new EnchantItemHandler();
        handlerInventoryClick = new InventoryClickHandler();
        handlerClickPlayer = new ClickPlayerHandler();
        commandHandler = new CommandHandler();

        handlerPlayerInteract.Init(this);
        handlerBlockBreak.Init(this);
        handlerCraftItem.Init(this);
        handlerEnchantItem.Init(this);
        handlerInventoryClick.Init(this);
        handlerClickPlayer.Init(this);
        commandHandler.Init(this);

        getLogger().info("JodellePowerMining plugin was enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PowerMining plugin was disabled.");
    }

    // ====================================================================
    //                         FILE GENERATION METHODS
    // ====================================================================

    // Note: These use saveResource(fileName, false) to copy the default from
    // the JAR if the file does not already exist in the plugin folder.

    private void generateDefaultRecipesFile() {
        saveResource("recipes.json", false);
        getLogger().info("Checking for recipes.json... File ready!");
    }

    private void generateDefaultConfig() {
        saveResource("config.json", false);
        getLogger().info("Checking for config.json... File ready!");
    }

    private void generateMineableFile() {
        saveResource("mineable.json", false);
        getLogger().info("Checking for mineable.json... File ready!");
    }

    private void generateDiggableFile() {
        saveResource("diggable.json", false);
        getLogger().info("Checking for diggable.json... File ready!");
    }

    // ====================================================================
    //                           FILE LOADING METHODS
    // ====================================================================

    /**
     * Loads the recipes.json file into a FileConfiguration object.
     * @return true if loading was successful, false otherwise.
     */
    private boolean loadRecipesConfig() {
        recipesFile = new File(getDataFolder(), "recipes.json");
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);

        if (recipesConfig.get("Recipes") == null) {
            getLogger().severe("recipes.json loaded but is missing the root 'Recipes' key. Check file structure.");
            return false;
        }

        getLogger().info("Successfully loaded recipes.json.");
        return true;
    }

    /**
     * Loads the config.json file into the generalConfig object.
     * @return true if a core key ("Radius") is found, false otherwise.
     */
    private boolean loadConfigFile() {
        File configFile = new File(getDataFolder(), "config.json");
        this.generalConfig = YamlConfiguration.loadConfiguration(configFile);

        if (!this.generalConfig.contains("Radius")) {
            getLogger().severe("config.json loaded but is missing the 'Radius' key. Check file structure.");
            return false;
        }

        getLogger().info("Successfully loaded config.json.");
        return true;
    }

    /**
     * Loads the mineable.json file into the mineableConfig object.
     * @return true if the root section ("Minable") is found, false otherwise.
     */
    private boolean loadMineableFile() {
        File mineableFile = new File(getDataFolder(), "mineable.json");
        this.mineableConfig = YamlConfiguration.loadConfiguration(mineableFile);

        if (this.mineableConfig.getConfigurationSection("Minable") == null) {
            getLogger().severe("mineable.json loaded but is missing the root 'Minable' key. Block mining rules will not apply.");
            return false;
        }

        getLogger().info("Successfully loaded mineable.json.");
        return true;
    }

    /**
     * Loads the diggable.json file into the diggableConfig object.
     * @return true if the root key ("Diggable") is found and not empty, false otherwise.
     */
    private boolean loadDiggableFile() {
        File diggableFile = new File(getDataFolder(), "diggable.json");
        this.diggableConfig = YamlConfiguration.loadConfiguration(diggableFile);

        List<String> diggableList = this.diggableConfig.getStringList("Diggable");

        if (diggableList == null) {
            getLogger().severe("diggable.json loaded but is missing the 'Diggable' list key. Check file structure.");
            return false;
        }

        if (diggableList.isEmpty()) {
            getLogger().warning("diggable.json loaded but the 'Diggable' list is empty.");
        }

        getLogger().info("Successfully loaded diggable.json.");
        return true;
    }


    // ====================================================================
    //                        CONFIGURATION PROCESSING
    // ====================================================================

    /**
     * Reads the recipes.json file, processes each recipe, and stores it in its respective HashMap.
     */
    private void processCraftingRecipes() {
        boolean showDebugMessage = false;
        ConfigurationSection recipesSection = this.recipesConfig.getConfigurationSection("Recipes");

        // This check is redundant due to the load method, but kept for safety.
        if (recipesSection == null) {
            getLogger().severe("The 'Recipes' section is missing or invalid in recipes.json. Cannot load recipes.");
            return;
        }

        for (String toolName : recipesSection.getKeys(false)) {
            debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.BLUE + "Processing " + toolName + " recipe");
            List<String> materialsList = recipesSection.getStringList(toolName);

            if (materialsList.size() != 9) {
                getLogger().warning("Recipe for tool '" + toolName + "' has an invalid size (" + materialsList.size() + "). Skipping.");
                continue;
            }

            ItemStack[] craftingRecipe = new ItemStack[9];
            int i = 0;
            boolean failed = false;

            for (String material : materialsList) {
                if (i >= 9) break;

                if (material.equals("EMPTY")) {
                    craftingRecipe[i] = null;
                    i++;
                    continue;
                }

                int separator = material.indexOf('*');
                if (separator == -1) {
                    getLogger().severe("Invalid format for '" + material + "' in recipe " + toolName + ". Missing '*'. Skipping recipe.");
                    failed = true;
                    break;
                }

                String materialNameStr = material.substring(0, separator);
                Material materialName = Material.getMaterial(materialNameStr);
                String quantityStr = material.substring(separator + 1);
                int quantity;

                if (materialName == null) {
                    getLogger().severe("Unknown material '" + materialNameStr + "' in recipe " + toolName + ". Skipping recipe.");
                    failed = true;
                    break;
                }

                try {
                    quantity = Integer.parseInt(quantityStr);
                } catch (NumberFormatException e) {
                    getLogger().severe("Invalid quantity '" + quantityStr + "' for material " + materialNameStr + " in recipe " + toolName + ". Skipping recipe.");
                    failed = true;
                    break;
                }

                ItemStack itemStack = new ItemStack(materialName, quantity);

                if (quantity > itemStack.getMaxStackSize()) {
                    getLogger().severe("Recipe " + toolName + " is invalid: Quantity (" + quantity + ") exceeds max stack size (" + itemStack.getMaxStackSize() + ") for " + materialNameStr);
                    failed = true;
                    break;
                }

                craftingRecipe[i] = itemStack;
                debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.GOLD + "Material: " + material);
                i++;
            }

            if (failed) {
                getLogger().severe("Recipe " + toolName + " failed validation and was not stored.");
                continue;
            }

            // 5. Store the processed recipe array in the correct Reference HashMap
            if (Reference.HAMMERS.contains(toolName)) {
                Reference.HAMMER_CRAFTING_RECIPES.put(toolName, craftingRecipe);
                debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.RED + toolName + " recipe processed successfully");
            } else if (Reference.EXCAVATORS.contains(toolName)) {
                Reference.EXCAVATOR_CRAFTING_RECIPES.put(toolName, craftingRecipe);
                debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.RED + toolName + " recipe processed successfully");
            } else if (Reference.PLOWS.contains(toolName)) {
                Reference.PLOW_CRAFTING_RECIPES.put(toolName, craftingRecipe);
                debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.RED + toolName + " recipe processed successfully");
            } else {
                getLogger().warning("Tool name '" + toolName + "' found in recipes.json but not defined in Reference constants. Recipe ignored.");
            }
        }
    }


    /**
     * Reads the config.json, mineable.json, and diggable.json files and processes them.
     */
    public void processConfig() {
        // ----------------------------------------------
        // 1. PROCESS MINABLE BLOCKS (from mineable.json)
        // ----------------------------------------------
        try {
            ConfigurationSection minableSection = this.mineableConfig.getConfigurationSection("Minable");

            if (minableSection == null) return; // Already logged in loadMineableFile

            for (String blockType : minableSection.getKeys(false)) {
                if (blockType == null || blockType.isEmpty()) continue;

                Material blockMaterial = Material.getMaterial(blockType);

                if (blockMaterial == null || Reference.MINABLE.containsKey(blockMaterial)) continue;

                List<String> requiredTools = minableSection.getStringList(blockType);

                Reference.MINABLE.put(blockMaterial, new ArrayList<>());
                ArrayList<Material> temp = Reference.MINABLE.get(blockMaterial);

                if (requiredTools.contains("any")) {
                    temp = null;
                } else {
                    for (String hammerType : requiredTools) {
                        if (hammerType == null || hammerType.isEmpty()) continue;

                        Material hammerMaterial = Material.getMaterial(hammerType);

                        if (hammerMaterial == null || (temp != null && temp.contains(hammerMaterial))) continue;

                        if (temp != null) temp.add(hammerMaterial);
                    }
                }
                Reference.MINABLE.put(blockMaterial, temp);
            }
        } catch (Exception e) {
            getLogger().severe("Error reading Minable list from mineable.json: " + e.getMessage());
        }

        // ----------------------------------------------
        // 2. PROCESS DIGGABLE BLOCKS (from diggable.json)
        // ----------------------------------------------
        try {
            List<String> diggableBlocks = this.diggableConfig.getStringList("Diggable");

            // Empty check already logged in loadDiggableFile()

            for (String blockType : diggableBlocks) {
                if (blockType == null || blockType.isEmpty()) continue;

                Material blockMaterial = Material.getMaterial(blockType);

                if (blockMaterial != null && !Reference.DIGGABLE.contains(blockMaterial))
                    Reference.DIGGABLE.add(blockMaterial);
            }
        } catch (Exception e) {
            getLogger().severe("Error reading Diggable list from diggable.json: " + e.getMessage());
        }

        // ----------------------------------------------
        // 3. PROCESS GENERAL SETTINGS (from config.json)
        // ----------------------------------------------
        try {
            // Note: Since Reference.RADIUS is now a primitive int with a default,
            // a config error here won't crash the plugin, but we log it anyway.
            Reference.RADIUS = this.generalConfig.getInt("Radius");
            Reference.DEEP = this.generalConfig.getInt("Deep");

            // Log for verification
            getLogger().info("Loaded Radius: " + Reference.RADIUS + ", Deep: " + Reference.DEEP);

        } catch (Exception e) {
            getLogger().severe("Error reading Radius/Deep from config.json: " + e.getMessage());
            getLogger().info("Using default values (Radius=1, Deep=0).");
        }
    }


    // ====================================================================
    //                           UTILITY METHODS
    // ====================================================================

    /**
     * Loads the dependencies that the plugin might require to properly function
     */
    private void loadDependencies() {
        boolean debugging = true;

        debuggingMessages.sendConsoleMessage(debugging, ChatColor.YELLOW + "Loading dependencies...");

        for (String pluginName : Reference.dependencies) {
            Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
            if (plugin instanceof WorldGuardPlugin){
                debuggingMessages.sendConsoleMessage(debugging, ChatColor.YELLOW + pluginName + " Found!");
                worldguard = (WorldGuardPlugin) plugin;
            }
        }
    }

    /**
     * Fills the permissions HashMaps with the available permissions.
     */
    private void processPermissions() {

        debuggingMessages.sendConsoleMessage(ChatColor.GOLD + "[JodellePowerMining] - Setting up Permissions");
        generatePermission(Reference.HAMMERS, Reference.PICKAXES);
        generatePermission(Reference.EXCAVATORS, Reference.SHOVELS);
        generatePermission(Reference.PLOWS, Reference.HOES);

        debuggingMessages.sendConsoleMessage(String.valueOf(Reference.CRAFT_PERMISSIONS.size()));

        for (Map.Entry<Material, String> materialStringEntry : Reference.USE_PERMISSIONS.entrySet()) {
            debuggingMessages.sendConsoleMessage(ChatColor.GOLD + "Material: " + materialStringEntry.getKey().toString() + " - Permission " + materialStringEntry.getValue());
        }

    }

    /**
     * Generates the permissions in form of a String
     */
    protected void generatePermission(@Nonnull final ArrayList<String> powerToolNames, @Nonnull final ArrayList<Material> items) {
        int i = 0;
        for (String tool : powerToolNames) {
            String toolType = tool.substring(tool.indexOf("_") + 1).toLowerCase();
            String toolMaterial = tool.substring(0, tool.indexOf("_")).toLowerCase();

            String craftPermission = "powermining.craft." + toolType + "." + toolMaterial;
            String usePermission = "powermining.use." + toolType + "." + toolMaterial;
            String enchantPermission = "powermining.enchant." + toolType + "." + toolMaterial;

            Reference.CRAFT_PERMISSIONS.put(items.get(i), craftPermission);
            Reference.USE_PERMISSIONS.put(items.get(i), usePermission);
            Reference.ENCHANT_PERMISSIONS.put(items.get(i), enchantPermission);
            i++;
        }
    }

    // ====================================================================
    //                           GETTER METHODS
    // ====================================================================

    public PlayerInteractHandler getPlayerInteractHandler() {
        return handlerPlayerInteract;
    }

    public BlockBreakHandler getBlockBreakHandler() {
        return handlerBlockBreak;
    }

    public ClickPlayerHandler getHandlerClickPlayer() {
        return handlerClickPlayer;
    }

    public CraftItemHandler getCraftItemHandler() {
        return handlerCraftItem;
    }

    public EnchantItemHandler getEnchantItemHandler() {
        return handlerEnchantItem;
    }

    public InventoryClickHandler getInventoryClickHandler() {
        return handlerInventoryClick;
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldguard;
    }

    public DebuggingMessages getDebuggingMessages() {
        return debuggingMessages;
    }

    public static PowerMining getInstance() {
        return instance;
    }
}
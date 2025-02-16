package jodelle.powermining;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import jodelle.powermining.commands.PowerMiningCommand;
import jodelle.powermining.commands.PowerMiningTabCompleter;
import jodelle.powermining.handlers.*;

import jodelle.powermining.integrations.JobsHook;
import jodelle.powermining.integrations.JobsHookImpl;

import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import jodelle.powermining.listeners.BlockBreakListener;
import jodelle.powermining.managers.ConfigMigrator;
import jodelle.powermining.managers.RecipeManager;
import jodelle.powermining.utils.LangFile;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class for the PowerMining plugin.
 * 
 * <p>
 * This class extends {@link JavaPlugin} and serves as the entry point for the
 * PowerMining Bukkit plugin. It initializes configuration settings, registers
 * event listeners, handles dependencies, and manages the plugin lifecycle.
 * </p>
 * 
 * <p>
 * PowerMining enhances the mining experience by adding specialized PowerTools
 * such as Hammers, Excavators, and Plows, each with unique abilities for
 * breaking
 * blocks efficiently.
 * </p>
 */
public final class PowerMining extends JavaPlugin {
    public JavaPlugin plugin;
    private PlayerInteractHandler handlerPlayerInteract;
    private BlockBreakHandler handlerBlockBreak;
    private CraftItemHandler handlerCraftItem;
    private EnchantItemHandler handlerEnchantItem;
    private InventoryClickHandler handlerInventoryClick;
    private ClickPlayerHandler handlerClickPlayer;
    private AnvilRepairHandler handlerAnvilRepair;
    private RecipeManager recipeManager;
    private DebuggingMessages debuggingMessages;
    private LangFile langFile;
    private BlockBreakListener blockBreakListener;
    public File ConfigFile = new File(getDataFolder(), "config.yml");
    private List<String> availableLanguages = new ArrayList<>();

    private WorldGuardPlugin worldguard;
    private JobsHook jobsHook = null;

    private boolean isJobsLoaded;

    private static PowerMining instance;
    String prefix = ChatColor.DARK_BLUE + "[" + ChatColor.DARK_GREEN + "Jodelle" + ChatColor.DARK_RED + "Power"
            + ChatColor.GOLD + "Mining"
            + ChatColor.DARK_BLUE + "]" + ChatColor.RESET + " ";

    /**
     * Called when the plugin is enabled.
     * 
     * <p>
     * This method initializes the plugin, registers event listeners, processes
     * configuration files, loads dependencies, and sets up necessary handlers.
     * Additionally, it measures the startup time and prints a message to the
     * console indicating the time taken to enable the plugin.
     * </p>
     * 
     * <p>
     * Key startup actions:
     * </p>
     * <ul>
     * <li>Registers the plugin with bStats for tracking usage statistics.</li>
     * <li>Ensures the configuration and language files exist.</li>
     * <li>Loads permissions and processes the configuration.</li>
     * <li>Initializes event handlers and command executors.</li>
     * <li>Checks for external dependencies like WorldGuard and Jobs.</li>
     * <li>Registers crafting recipes for PowerTools.</li>
     * </ul>
     */
    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        // bstats
        int pluginId = 24587;
        new Metrics(this, pluginId);

        instance = this;
        ensureConfigExists();
        ensureLanguageFilesExist();

        // Load available languages for TabCompleter
        loadAvailableLanguages();

        // Initialize DebuggingMessages early to prevent NullPointerException
        this.debuggingMessages = new DebuggingMessages();
        new ConfigMigrator(this).migrateConfig();

        // Load Language
        String language = getConfig().getString("language", "en_US");
        langFile = new LangFile(this, language);

        processConfig();
        this.recipeManager = new RecipeManager(this);
        recipeManager.registerRecipes();
        processPermissions();
        getLogger().info("Finished processing config file.");
        loadDependencies();

        // Initialize Jobs integration if Jobs is loaded
        if (isJobsLoaded) {
            jobsHook = new JobsHookImpl();
        }

        handlerPlayerInteract = new PlayerInteractHandler();
        handlerBlockBreak = new BlockBreakHandler();
        handlerCraftItem = new CraftItemHandler();
        handlerEnchantItem = new EnchantItemHandler();
        handlerInventoryClick = new InventoryClickHandler();
        handlerClickPlayer = new ClickPlayerHandler();
        handlerAnvilRepair = new AnvilRepairHandler();

        handlerPlayerInteract.Init(this);
        handlerBlockBreak.Init(this);
        handlerCraftItem.Init(this);
        handlerEnchantItem.Init(this);
        handlerInventoryClick.Init(this);
        handlerClickPlayer.Init(this);
        handlerAnvilRepair.Init(this);

        this.registerCommand();

        // Messure Time for the complete Startup
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        Bukkit.getServer().getConsoleSender().sendMessage(
                prefix + ChatColor.GREEN + "Plugin enabled in " + ChatColor.GOLD + duration + "ms" + ChatColor.RESET);
    }

    /**
     * Called when the plugin is disabled.
     * 
     * <p>
     * This method ensures that critical data, such as configuration and language
     * files, are preserved before the plugin shuts down. It also unregisters any
     * custom recipes that were added during plugin runtime.
     * </p>
     * 
     * <p>
     * Key shutdown actions:
     * </p>
     * <ul>
     * <li>Ensures the configuration and language files remain intact.</li>
     * <li>Unregisters custom recipes to prevent conflicts on reload.</li>
     * <li>Logs a shutdown message to indicate the plugin has been disabled.</li>
     * </ul>
     */
    @Override
    public void onDisable() {
        // Save config / language and schematic in case it got deleted somehow
        this.ensureConfigExists();
        this.ensureLanguageFilesExist();

        // Unregister Recipes
        if (recipeManager != null) {
            recipeManager.unregisterRecipes();
            getLogger().info("PowerMining recipes unregistered successfully!");
        }

        getLogger().info("PowerMining plugin was disabled.");
    }
    
    /**
     * Loads the dependencies that the plugin might require to properly function.
     * 
     * <p>
     * This method checks for the presence of required dependencies such as
     * WorldGuard and Jobs. If found, it initializes them for use within the plugin.
     * </p>
     */
    private void loadDependencies() {
        debuggingMessages.sendConsoleMessage(ChatColor.YELLOW + "Loading dependencies...");

        for (String pluginName : Reference.dependencies) {
            Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);

            if (plugin == null) {
                debuggingMessages.sendConsoleMessage(ChatColor.RED + pluginName + " not found.");
                continue;
            }

            if (plugin instanceof WorldGuardPlugin) {
                debuggingMessages.sendConsoleMessage(ChatColor.YELLOW + "WorldGuard Found!");
                worldguard = (WorldGuardPlugin) plugin;
            } else if (pluginName.equals("Jobs")) {
                debuggingMessages.sendConsoleMessage(ChatColor.YELLOW + "Jobs Found!");
                isJobsLoaded = true;
            }
        }
    }

    /**
     * Populates the permissions HashMaps with the available permissions.
     * 
     * <p>
     * This method initializes the permission mappings for crafting, using, and
     * enchanting
     * PowerTools by iterating over the defined PowerTool types.
     * </p>
     */
    private void processPermissions() {

        debuggingMessages.sendConsoleMessage(ChatColor.GOLD + "[JodellePowerMining] - Setting up Permissions");

        generatePermission(Reference.HAMMERS, Reference.PICKAXES);
        generatePermission(Reference.EXCAVATORS, Reference.SHOVELS);
        generatePermission(Reference.PLOWS, Reference.HOES);

        debuggingMessages.sendConsoleMessage(String.valueOf(Reference.CRAFT_PERMISSIONS.size()));

        for (Map.Entry<Material, String> materialStringEntry : Reference.USE_PERMISSIONS.entrySet()) {
            debuggingMessages.sendConsoleMessage(ChatColor.GOLD + "Material: " + materialStringEntry.getKey().toString()
                    + " - Permission " + materialStringEntry.getValue());
        }
    }

    /**
     * Generates the necessary permissions for a set of PowerTools.
     * 
     * <p>
     * This method constructs permission strings for crafting, using, and enchanting
     * tools based on their names and assigns them to the corresponding material in
     * the permission mappings.
     * </p>
     * 
     * @param powerToolNames List of PowerTool names.
     * @param items          List of corresponding {@link Material} types.
     */
    protected void generatePermission(@Nonnull final ArrayList<String> powerToolNames,
            @Nonnull final ArrayList<Material> items) {

        int i = 0;
        for (String tool : powerToolNames) {
            String craftPermission = "powermining.craft." + tool.substring(tool.indexOf("_") + 1).toLowerCase() + "."
                    + tool.substring(0, tool.indexOf("_")).toLowerCase();
            String usePermission = "powermining.use." + tool.substring(tool.indexOf("_") + 1).toLowerCase() + "."
                    + tool.substring(0, tool.indexOf("_")).toLowerCase();
            String enchantPermission = "powermining.enchant." + tool.substring(tool.indexOf("_") + 1).toLowerCase()
                    + "." + tool.substring(0, tool.indexOf("_")).toLowerCase();

            Reference.CRAFT_PERMISSIONS.put(items.get(i), craftPermission);
            Reference.USE_PERMISSIONS.put(items.get(i), usePermission);
            Reference.ENCHANT_PERMISSIONS.put(items.get(i), enchantPermission);
            i++;
        }

    }

    /**
     * Reads and processes the configuration file.
     * 
     * <p>
     * This method extracts various settings from the config file, including
     * the list of minable, diggable, tillable, and pathable blocks, and sets
     * up radius and depth values for PowerTools.
     * </p>
     */
    public void processConfig() {
        try {
            for (Object x : (ArrayList<?>) getConfig().getList("Minable")) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, ArrayList<String>> l = (LinkedHashMap<String, ArrayList<String>>) x;

                for (String blockType : l.keySet()) {
                    if (blockType == null || blockType.isEmpty())
                        continue;

                    if (Material.getMaterial(blockType) == null
                            || Reference.MINABLE.containsKey(Material.getMaterial(blockType)))
                        continue;

                    Reference.MINABLE.put(Material.getMaterial(blockType), new ArrayList<>());
                    ArrayList<Material> temp = Reference.MINABLE.get(Material.getMaterial(blockType));

                    for (String hammerType : l.get(blockType)) {
                        if (hammerType == null || hammerType.isEmpty())
                            continue;

                        if (hammerType.equals("any"))
                            temp = null;

                        if (hammerType != null && (Material.getMaterial(hammerType) == null ||
                                (temp != null && temp.contains(Material.getMaterial(hammerType)))))
                            continue;

                        if (temp != null)
                            temp.add(Material.getMaterial(hammerType));
                    }

                    Reference.MINABLE.put(Material.getMaterial(blockType), temp);
                }
            }
        } catch (NullPointerException e) {
            getLogger().info(
                    "NPE when trying to read the Minable list from the config file, check if it's set correctly!");
        }

        try {
            for (String blockType : getConfig().getStringList("Diggable")) {
                if (blockType == null || blockType.isEmpty())
                    continue;

                if (Material.getMaterial(blockType) != null
                        && !Reference.DIGGABLE.contains(Material.getMaterial(blockType)))
                    Reference.DIGGABLE.add(Material.getMaterial(blockType));
            }
        } catch (NullPointerException e) {
            getLogger().info(
                    "NPE when trying to read the Digable list from the config file, check if it's set correctly!");
        }

        // Register for tools (Substracting 1, because Java starts with 0 (easier for
        // user))
        try {
            Reference.RADIUS = getConfig().getInt("Radius") - 1;
            Reference.DEPTH = getConfig().getInt("Depth") - 1;
            debuggingMessages.sendConsoleMessage("(processConfig) Value for radius is: (Reference) " + Reference.RADIUS
                    + " (Config) " + (getConfig().getInt("Radius", 2) - 1));
            debuggingMessages.sendConsoleMessage("(processConfig) Value for depth is: (Reference) " + Reference.DEPTH
                    + " (Config) " + (getConfig().getInt("Depth", 1) - 1));
        } catch (NullPointerException e) {
            getLogger().info("HOE, check if hoes radius is currectly added.");
        }
    }

    /**
     * Ensures that the configuration file exists, creating it if necessary.
     * 
     * <p>
     * If the configuration file is missing, this method extracts the default
     * `config.yml` from the plugin's resources.
     * </p>
     */
    public void ensureConfigExists() {
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    /**
     * Ensures that the necessary language files exist.
     * 
     * <p>
     * This method creates the language directory and extracts default language
     * files if they are missing.
     * </p>
     */
    public void ensureLanguageFilesExist() {
        File langDir = new File(this.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Check and extract 'en_US.yml' if it doesn't exist
        File enLangFile = new File(langDir, "en_US.yml");
        if (!enLangFile.exists()) {
            saveResource("lang/en_US.yml", false);
        }

        // Check and extract 'de_DE.yml' if it doesn't exist
        File deLangFile = new File(langDir, "de_DE.yml");
        if (!deLangFile.exists()) {
            saveResource("lang/de_DE.yml", false);
        }
    }

    /**
     * Checks whether a specific language file exists.
     * 
     * <p>
     * This method verifies the existence of a language file and ensures that
     * it contains the required configuration keys.
     * </p>
     * 
     * @param lang The language code (e.g., "en_US").
     * @return {@code true} if the language file exists and is valid, {@code false}
     *         otherwise.
     */
    public boolean doesLanguageExist(String lang) {
        // Define the directory and the language file
        File langDir = new File(getDataFolder(), "lang");
        File langFile = new File(langDir, lang + ".yml");

        // Check if the language file exists
        if (!langFile.exists()) {
            return false; // Language file does not exist
        }

        // Load the language file
        FileConfiguration langConfig = new YamlConfiguration();
        try {
            langConfig.load(langFile);
        } catch (IOException | InvalidConfigurationException e) {
            // Log the error and return false if the file is malformed or can't be read
            getLogger().severe(prefix + "Could not load language file " + lang + ".yml: " + e.getMessage());
            return false;
        }

        // Check if all required keys exist in the file
        return langConfig.contains("prefix") &&
                langConfig.contains("plugin-info") &&
                langConfig.contains("powermining-cmd-not-found");
    }

    /**
     * Retrieves the {@link RecipeManager} instance.
     * 
     * @return The {@link RecipeManager} instance.
     */
    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /**
     * Unregisters the RecipeManager and removes custom recipes.
     */
    public void unregisterRecipeManager() {
        if (recipeManager != null) {
            recipeManager.unregisterRecipes();
            getLogger().info("PowerMining recipes unregistered successfully!");
        } else {
            getLogger().warning("RecipeManager is null, skipping unregistering.");
        }
    }

    /**
     * Registers the RecipeManager and adds custom recipes.
     */
    public void registerRecipeManager() {
        if (recipeManager == null) {
            recipeManager = new RecipeManager(this);
        }
        recipeManager.registerRecipes();
        getLogger().info("PowerMining recipes registered successfully!");
    }

    /**
     * Registers the command and tab completer for PowerMining.
     * 
     * <p>
     * This method sets up the "/powermining" command with its executor and
     * tab completer.
     * </p>
     */
    public void registerCommand() {
        // Register Command
        this.getCommand("powermining").setExecutor(new PowerMiningCommand(this));
        this.getCommand("powermining").setTabCompleter(new PowerMiningTabCompleter(this));
    }

    /**
     * Loads the available languages from the language directory.
     * 
     * <p>
     * This method scans the "lang" folder for language files and caches
     * their names for use in command auto-completion.
     * </p>
     */
    public void loadAvailableLanguages() {
        File langFolder = new File(getDataFolder(), "lang");
        if (langFolder.exists() && langFolder.isDirectory()) {
            File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                availableLanguages = new ArrayList<>();
                for (File file : files) {
                    availableLanguages.add(file.getName().replace(".yml", ""));
                }
            }
        }
    }

    /**
     * Retrieves the cached list of available languages.
     * 
     * @return A list of available language codes.
     */
    public List<String> getAvailableLanguagesFromCache() {
        return availableLanguages;
    }

    /**
     * Refreshes the DebuggingMessages instance.
     * 
     * <p>
     * This method reinitializes the DebuggingMessages object to ensure
     * updated debug configurations are applied.
     * </p>
     */
    public void refreshDebuggingMessages() {
        this.debuggingMessages = new DebuggingMessages();
    }

    /**
     * Retrieves the {@link LangFile} instance.
     * 
     * @return The {@link LangFile} instance.
     */
    public LangFile getLangFile() {
        return langFile;
    }

    /**
     * Sets a new {@link LangFile} instance.
     * 
     * @param langFile The new {@link LangFile} instance.
     */
    public void setLangFile(LangFile langFile) {
        this.langFile = langFile;
    }

    /**
     * Checks if debug mode is enabled.
     * 
     * @return {@code true} if debug mode is enabled, {@code false} otherwise.
     */
    public static boolean isDebugMode() {
        return getInstance().getConfig().getBoolean("debug");
    }

    /**
     * Retrieves the {@link PlayerInteractHandler} instance.
     * 
     * @return The {@link PlayerInteractHandler} instance.
     */
    public PlayerInteractHandler getPlayerInteractHandler() {
        return handlerPlayerInteract;
    }

    /**
     * Retrieves the {@link BlockBreakHandler} instance.
     * 
     * @return The {@link BlockBreakHandler} instance.
     */
    public BlockBreakHandler getBlockBreakHandler() {
        return handlerBlockBreak;
    }

    /**
     * Retrieves the {@link ClickPlayerHandler} instance.
     * 
     * @return The {@link ClickPlayerHandler} instance.
     */
    public ClickPlayerHandler getHandlerClickPlayer() {
        return handlerClickPlayer;
    }

    /**
     * Retrieves the {@link CraftItemHandler} instance.
     * 
     * @return The {@link CraftItemHandler} instance.
     */
    public CraftItemHandler getCraftItemHandler() {
        return handlerCraftItem;
    }

    /**
     * Retrieves the {@link EnchantItemHandler} instance.
     * 
     * @return The {@link EnchantItemHandler} instance.
     */
    public EnchantItemHandler getEnchantItemHandler() {
        return handlerEnchantItem;
    }

    /**
     * Retrieves the {@link InventoryClickHandler} instance.
     * 
     * @return The {@link InventoryClickHandler} instance.
     */
    public InventoryClickHandler getInventoryClickHandler() {
        return handlerInventoryClick;
    }

    /**
     * Retrieves the WorldGuard plugin instance.
     * 
     * @return The {@link WorldGuardPlugin} instance, or {@code null} if not loaded.
     */
    public WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) worldguard;
    }

    /**
     * Retrieves the {@link DebuggingMessages} instance.
     * 
     * @return The {@link DebuggingMessages} instance.
     */
    public DebuggingMessages getDebuggingMessages() {
        return debuggingMessages;
    }

    /**
     * Retrieves the {@link BlockBreakListener} instance.
     * 
     * @return The {@link BlockBreakListener} instance.
     */
    public BlockBreakListener getBlockBreakListener() {
        return blockBreakListener;
    }

    /**
     * Checks whether the Jobs plugin is loaded.
     * 
     * @return {@code true} if Jobs is loaded, {@code false} otherwise.
     */
    public boolean isJobsLoaded() {
        return isJobsLoaded;
    }

    /**
     * Sets the {@link BlockBreakListener} instance.
     * 
     * @param listener The new {@link BlockBreakListener} instance.
     */
    public void setBlockBreakListener(BlockBreakListener listener) {
        this.blockBreakListener = listener;
    }

    /**
     * Retrieves the {@link JobsHook} instance.
     * 
     * @return The {@link JobsHook} instance, or {@code null} if Jobs is not loaded.
     */
    public JobsHook getJobsHook() {
        return jobsHook;
    }

    /**
     * Retrieves the singleton instance of the PowerMining plugin.
     * 
     * @return The {@link PowerMining} instance.
     */
    public static PowerMining getInstance() {
        return instance;
    }
}
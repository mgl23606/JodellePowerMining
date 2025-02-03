/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Main Plugin class, responsible for initializing the plugin and it's respective systems, also keeps a reference to the handlers
*/

package jodelle.powermining;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import jodelle.powermining.commands.PowerMiningCommand;
import jodelle.powermining.commands.PowerMiningTabCompleter;
import jodelle.powermining.handlers.*;
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

    private static PowerMining instance;
    String prefix = ChatColor.DARK_BLUE + "[" + ChatColor.DARK_GREEN + "Jodelle" + ChatColor.DARK_RED + "Power"
            + ChatColor.GOLD + "Mining"
            + ChatColor.DARK_BLUE + "]" + ChatColor.RESET + " ";

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
     * Loads the dependencies that the plugin might require to properly function
     */
    private void loadDependencies() {
        debuggingMessages.sendConsoleMessage(ChatColor.YELLOW + "Loading dependencies...");

        for (String pluginName : Reference.dependencies) {
            Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
            if (plugin instanceof WorldGuardPlugin) {
                debuggingMessages.sendConsoleMessage(ChatColor.YELLOW + pluginName + " Found!");
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
            debuggingMessages.sendConsoleMessage(ChatColor.GOLD + "Material: " + materialStringEntry.getKey().toString()
                    + " - Permission " + materialStringEntry.getValue());
        }
    }

    /**
     * Generates the permissions in form of a String
     * 
     * @param powerToolNames Array containing the names of all the PowerTools
     * @param items          List of the items
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
     * Reads the config file and processes it
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

    // Ensure the config.yml exists when reloading the plugin
    public void ensureConfigExists() {
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    // Ensure Language files exists when reloading the plugin
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

    // Check if Language File exists
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

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public void unregisterRecipeManager() {
        if (recipeManager != null) {
            recipeManager.unregisterRecipes();
            getLogger().info("PowerMining recipes unregistered successfully!");
        } else {
            getLogger().warning("RecipeManager is null, skipping unregistering.");
        }
    }

    public void registerRecipeManager() {
        if (recipeManager == null) {
            recipeManager = new RecipeManager(this);
        }
        recipeManager.registerRecipes();
        getLogger().info("PowerMining recipes registered successfully!");
    }

    public void registerCommand() {
        // Register Command
        this.getCommand("powermining").setExecutor(new PowerMiningCommand(this));
        this.getCommand("powermining").setTabCompleter(new PowerMiningTabCompleter(this));
    }

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

    public List<String> getAvailableLanguagesFromCache() {
        return availableLanguages;
    }

    public void refreshDebuggingMessages() {
        this.debuggingMessages = new DebuggingMessages();
    }

    // Methode to get the LangFile
    public LangFile getLangFile() {
        return langFile;
    }

    // Methode to set the LangFile
    public void setLangFile(LangFile langFile) {
        this.langFile = langFile;
    }

    // Methode to set Debug
    public static boolean isDebugMode() {
        return getInstance().getConfig().getBoolean("debug");
    }

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
        return (WorldGuardPlugin) worldguard;
    }

    public DebuggingMessages getDebuggingMessages() {
        return debuggingMessages;
    }

    public BlockBreakListener getBlockBreakListener() {
        return blockBreakListener;
    }

    public void setBlockBreakListener(BlockBreakListener listener) {
        this.blockBreakListener = listener;
    }

    public static PowerMining getInstance() {
        return instance;
    }
}

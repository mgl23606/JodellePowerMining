/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jodelle.powermining.handlers.*;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class PowerMining extends JavaPlugin {

    private PlayerInteractHandler handlerPlayerInteract;
    private BlockBreakHandler handlerBlockBreak;
    private CraftItemHandler handlerCraftItem;
    private EnchantItemHandler handlerEnchantItem;
    private InventoryClickHandler handlerInventoryClick;
    private ClickPlayerHandler handlerClickPlayer;
    private CommandHandler commandHandler;
    private DebuggingMessages debuggingMessages;

    private WorldGuardPlugin worldguard; // Optional — may be null if WG is not present
    private static PowerMining instance;

    // Config JSON objects
    private JsonObject recipesJson;
    private JsonObject configJson;
    private JsonObject mineableJson;
    private JsonObject diggableJson;

    private final Gson gson = new Gson();

    @Override
    public void onEnable() {
        if (instance != null) {
            getLogger().warning("PowerMining instance already exists. Resetting instance reference.");
        }
        instance = this;

        debuggingMessages = new DebuggingMessages();

        // --- FILE GENERATION ---
        generateDefaultFiles();

        // --- LOAD JSON CONFIGS ---
        if (!loadJsonFiles()) {
            getLogger().severe("Failed to load JSON config files. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- PROCESS CONFIG DATA ---
        processConfig();
        processCraftingRecipes();
        processPermissions();
        getLogger().info("All JSON configuration files processed successfully.");

        // --- DEPENDENCIES ---
        loadDependencies();

        // --- REGISTER HANDLERS ---
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

        getLogger().info("PowerMining plugin enabled successfully for Minecraft 1.21.10!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PowerMining plugin was disabled.");
    }

    // ====================================================================
    // FILE CREATION
    // ====================================================================

    private void generateDefaultFiles() {
        createFileIfMissing("recipes.json");
        createFileIfMissing("config.json");
        createFileIfMissing("mineable.json");
        createFileIfMissing("diggable.json");
        getLogger().info("All JSON configuration files verified.");
    }

    /**
     * Creates a file from the JAR if it doesn't exist, otherwise silently skips.
     */
    private void createFileIfMissing(String fileName) {
        File target = new File(getDataFolder(), fileName);
        if (target.exists()) return; // file already there, no warnings

        getDataFolder().mkdirs();
        try (InputStream in = getResource(fileName)) {
            if (in != null) {
                try (OutputStream out = new FileOutputStream(target)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                }
                getLogger().info("Created default " + fileName);
            } else {
                // No bundled version found — make empty file
                target.createNewFile();
                getLogger().warning(fileName + " not found in JAR; created empty file.");
            }
        } catch (IOException e) {
            getLogger().severe("Error creating " + fileName + ": " + e.getMessage());
        }
    }

    // ====================================================================
    // JSON LOADING
    // ====================================================================

    private boolean loadJsonFiles() {
        recipesJson = loadJson("recipes.json");
        configJson = loadJson("config.json");
        mineableJson = loadJson("mineable.json");
        diggableJson = loadJson("diggable.json");

        if (recipesJson == null || configJson == null || mineableJson == null || diggableJson == null) {
            getLogger().severe("One or more JSON configuration files failed to load.");
            return false;
        }

        if (!recipesJson.has("Recipes")) {
            getLogger().severe("recipes.json missing 'Recipes' root element.");
            return false;
        }
        if (!configJson.has("Radius")) {
            getLogger().severe("config.json missing 'Radius' key.");
            return false;
        }
        if (!mineableJson.has("Minable")) {
            getLogger().severe("mineable.json missing 'Minable' root element.");
            return false;
        }
        if (!diggableJson.has("Diggable")) {
            getLogger().severe("diggable.json missing 'Diggable' array.");
            return false;
        }

        return true;
    }

    private JsonObject loadJson(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            getLogger().warning(fileName + " not found, creating empty object.");
            return new JsonObject();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                getLogger().info("Loaded " + fileName);
                return element.getAsJsonObject();
            } else {
                getLogger().severe(fileName + " does not contain a valid JSON object!");
                return null;
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    // ====================================================================
    // CONFIG PROCESSING
    // ====================================================================

    private void processCraftingRecipes() {
        JsonObject recipesSection = recipesJson.getAsJsonObject("Recipes");

        for (String toolName : recipesSection.keySet()) {
            JsonArray materialsList = recipesSection.getAsJsonArray(toolName);
            if (materialsList.size() != 9) {
                getLogger().warning("Recipe for " + toolName + " has invalid size (" + materialsList.size() + "). Skipping.");
                continue;
            }

            ItemStack[] recipe = new ItemStack[9];
            boolean failed = false;

            for (int i = 0; i < 9; i++) {
                String entry = materialsList.get(i).getAsString();
                if (entry.equalsIgnoreCase("EMPTY")) {
                    recipe[i] = null;
                    continue;
                }

                int sep = entry.indexOf('*');
                if (sep == -1) {
                    getLogger().severe("Invalid format for '" + entry + "' in " + toolName + ". Missing '*'.");
                    failed = true;
                    break;
                }

                String matName = entry.substring(0, sep);
                Material mat = Material.matchMaterial(matName, false);
                if (mat == null) {
                    getLogger().severe("Unknown material '" + matName + "' in recipe " + toolName);
                    failed = true;
                    break;
                }

                int qty;
                try {
                    qty = Integer.parseInt(entry.substring(sep + 1));
                } catch (NumberFormatException e) {
                    getLogger().severe("Invalid quantity for '" + entry + "' in " + toolName);
                    failed = true;
                    break;
                }

                recipe[i] = new ItemStack(mat, Math.min(qty, mat.getMaxStackSize()));
            }

            if (failed) continue;

            if (Reference.HAMMERS.contains(toolName))
                Reference.HAMMER_CRAFTING_RECIPES.put(toolName, recipe);
            else if (Reference.EXCAVATORS.contains(toolName))
                Reference.EXCAVATOR_CRAFTING_RECIPES.put(toolName, recipe);
            else if (Reference.PLOWS.contains(toolName))
                Reference.PLOW_CRAFTING_RECIPES.put(toolName, recipe);
            else
                getLogger().warning("Tool '" + toolName + "' not recognized. Skipping.");
        }
    }

    public void processConfig() {
        // --- Minable ---
        try {
            JsonObject section = mineableJson.getAsJsonObject("Minable");
            for (String block : section.keySet()) {
                Material blockMat = Material.matchMaterial(block, false);
                if (blockMat == null) continue;

                JsonArray tools = section.getAsJsonArray(block);
                if (tools == null) continue;

                List<String> toolList = new ArrayList<>();
                for (JsonElement e : tools) toolList.add(e.getAsString());

                if (toolList.contains("any")) {
                    Reference.MINABLE.put(blockMat, null);
                } else {
                    ArrayList<Material> list = new ArrayList<>();
                    for (String tool : toolList) {
                        Material toolMat = Material.matchMaterial(tool, false);
                        if (toolMat != null) list.add(toolMat);
                    }
                    Reference.MINABLE.put(blockMat, list);
                }
            }
        } catch (Exception e) {
            getLogger().severe("Error processing mineable.json: " + e.getMessage());
        }

        // --- Diggable ---
        try {
            JsonArray diggableArray = diggableJson.getAsJsonArray("Diggable");
            for (JsonElement element : diggableArray) {
                String block = element.getAsString();
                Material mat = Material.matchMaterial(block, false);
                if (mat != null && !Reference.DIGGABLE.contains(mat)) Reference.DIGGABLE.add(mat);
            }
        } catch (Exception e) {
            getLogger().severe("Error processing diggable.json: " + e.getMessage());
        }

        // --- General ---
        Reference.RADIUS = configJson.has("Radius") ? configJson.get("Radius").getAsInt() : 1;
        Reference.DEEP = configJson.has("Deep") ? configJson.get("Deep").getAsInt() : 0;
        getLogger().info("Config values loaded. Radius=" + Reference.RADIUS + ", Deep=" + Reference.DEEP);
    }

    // ====================================================================
    // DEPENDENCIES & PERMISSIONS
    // ====================================================================

    private void loadDependencies() {
        debuggingMessages.sendConsoleMessage(true, ChatColor.YELLOW + "Checking dependencies...");
        try {
            Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
            if (wg != null && wg.isEnabled() && wg instanceof WorldGuardPlugin) {
                worldguard = (WorldGuardPlugin) wg;
                debuggingMessages.sendConsoleMessage(true, ChatColor.GREEN + "WorldGuard (Bukkit plugin) found and hooked.");
            } else if (isWorldGuardAvailable()) {
                debuggingMessages.sendConsoleMessage(true, ChatColor.GREEN + "WorldGuard API available via WorldGuard.getInstance().");
            } else {
                worldguard = null;
                getLogger().warning("WorldGuard not found. Continuing without region protection.");
            }
        } catch (Throwable t) {
            worldguard = null;
            getLogger().warning("WorldGuard not installed or not accessible. Region features disabled.");
        }
    }

    private boolean isWorldGuardAvailable() {
        try {
            return WorldGuard.getInstance() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    private void processPermissions() {
        debuggingMessages.sendConsoleMessage(ChatColor.GOLD + "[PowerMining] - Setting up Permissions");
        generatePermission(Reference.HAMMERS, Reference.PICKAXES);
        generatePermission(Reference.EXCAVATORS, Reference.SHOVELS);
        generatePermission(Reference.PLOWS, Reference.HOES);
    }

    protected void generatePermission(@NotNull final ArrayList<String> powerToolNames,
                                      @NotNull final ArrayList<Material> items) {
        for (int i = 0; i < powerToolNames.size(); i++) {
            if (i >= items.size()) break;
            String tool = powerToolNames.get(i);
            String toolType = tool.substring(tool.indexOf("_") + 1).toLowerCase();
            String toolMaterial = tool.substring(0, tool.indexOf("_")).toLowerCase();

            String craft = "powermining.craft." + toolType + "." + toolMaterial;
            String use = "powermining.use." + toolType + "." + toolMaterial;
            String enchant = "powermining.enchant." + toolType + "." + toolMaterial;

            Material item = items.get(i);
            Reference.CRAFT_PERMISSIONS.put(item, craft);
            Reference.USE_PERMISSIONS.put(item, use);
            Reference.ENCHANT_PERMISSIONS.put(item, enchant);
        }
    }

    // ====================================================================
    // GETTERS
    // ====================================================================

    public PlayerInteractHandler getPlayerInteractHandler() { return handlerPlayerInteract; }
    public BlockBreakHandler getBlockBreakHandler() { return handlerBlockBreak; }
    public ClickPlayerHandler getHandlerClickPlayer() { return handlerClickPlayer; }
    public CraftItemHandler getCraftItemHandler() { return handlerCraftItem; }
    public EnchantItemHandler getEnchantItemHandler() { return handlerEnchantItem; }
    public InventoryClickHandler getInventoryClickHandler() { return handlerInventoryClick; }

    public WorldGuardPlugin getWorldGuard() { return worldguard; }

    public boolean hasWorldGuard() { return worldguard != null && worldguard.isEnabled(); }

    public DebuggingMessages getDebuggingMessages() { return debuggingMessages; }

    public static PowerMining getInstance() { return instance; }
}

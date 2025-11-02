/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining;

import com.google.gson.*;
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

    private WorldGuardPlugin worldguard; // Optional dependency
    private static PowerMining instance;

    // JSON config files
    private JsonObject recipesJson;
    private JsonObject configJson;
    private JsonObject mineableJson;
    private JsonObject diggableJson;

    private final Gson gson = new Gson();

    @Override
    public void onEnable() {
        instance = this;
        debuggingMessages = new DebuggingMessages();

        generateDefaultFiles();

        if (!loadJsonFiles()) {
            getLogger().severe("Failed to load JSON configuration. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        processConfig();
        processCraftingRecipes();
        processPermissions();

        loadDependencies();

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

    // =========================================================
    // FILE GENERATION
    // =========================================================
    private void generateDefaultFiles() {
        createFileIfMissing("recipes.json");
        createFileIfMissing("config.json");
        createFileIfMissing("mineable.json");
        createFileIfMissing("diggable.json");
    }

    private void createFileIfMissing(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (file.exists()) return;

        getDataFolder().mkdirs();
        try (InputStream in = getResource(fileName)) {
            if (in != null) {
                try (OutputStream out = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                getLogger().info("Created default " + fileName);
            } else {
                file.createNewFile();
                getLogger().warning(fileName + " not found in JAR; created empty file.");
            }
        } catch (IOException e) {
            getLogger().severe("Error creating " + fileName + ": " + e.getMessage());
        }
    }

    // =========================================================
    // JSON LOADING
    // =========================================================
    private boolean loadJsonFiles() {
        recipesJson = loadJson("recipes.json");
        configJson = loadJson("config.json");
        mineableJson = loadJson("mineable.json");
        diggableJson = loadJson("diggable.json");

        if (recipesJson == null || configJson == null || mineableJson == null || diggableJson == null)
            return false;

        return recipesJson.has("Recipes")
                && configJson.has("Radius")
                && mineableJson.has("Minable")
                && diggableJson.has("Diggable");
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
                getLogger().severe(fileName + " is not a valid JSON object!");
                return null;
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    // =========================================================
    // CONFIG PARSING
    // =========================================================
    private void processConfig() {
        try {
            JsonObject minableSection = mineableJson.getAsJsonObject("Minable");
            for (String block : minableSection.keySet()) {
                Material blockMat = Material.matchMaterial(block, false);
                if (blockMat == null) continue;

                JsonArray tools = minableSection.getAsJsonArray(block);
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

        try {
            JsonArray diggableArray = diggableJson.getAsJsonArray("Diggable");
            for (JsonElement element : diggableArray) {
                String block = element.getAsString();
                Material mat = Material.matchMaterial(block, false);
                if (mat != null && !Reference.DIGGABLE.contains(mat))
                    Reference.DIGGABLE.add(mat);
            }
        } catch (Exception e) {
            getLogger().severe("Error processing diggable.json: " + e.getMessage());
        }

        Reference.RADIUS = configJson.has("Radius") ? configJson.get("Radius").getAsInt() : 1;
        Reference.DEEP = configJson.has("Deep") ? configJson.get("Deep").getAsInt() : 0;

        getLogger().info("Loaded config: Radius=" + Reference.RADIUS + ", Deep=" + Reference.DEEP);
    }

    private void processCraftingRecipes() {
        JsonObject recipes = recipesJson.getAsJsonObject("Recipes");
        for (String tool : recipes.keySet()) {
            JsonArray mats = recipes.getAsJsonArray(tool);
            if (mats.size() != 9) {
                getLogger().warning("Invalid recipe for " + tool);
                continue;
            }

            ItemStack[] recipe = new ItemStack[9];
            boolean failed = false;

            for (int i = 0; i < 9; i++) {
                String entry = mats.get(i).getAsString();
                if (entry.equalsIgnoreCase("EMPTY")) continue;

                int sep = entry.indexOf('*');
                if (sep == -1) {
                    getLogger().severe("Invalid recipe entry '" + entry + "' in " + tool);
                    failed = true;
                    break;
                }

                String matName = entry.substring(0, sep);
                Material mat = Material.matchMaterial(matName, false);
                if (mat == null) {
                    getLogger().severe("Unknown material " + matName + " in " + tool);
                    failed = true;
                    break;
                }

                int qty = Integer.parseInt(entry.substring(sep + 1));
                recipe[i] = new ItemStack(mat, Math.min(qty, mat.getMaxStackSize()));
            }

            if (failed) continue;

            if (Reference.HAMMERS.contains(tool))
                Reference.HAMMER_CRAFTING_RECIPES.put(tool, recipe);
            else if (Reference.EXCAVATORS.contains(tool))
                Reference.EXCAVATOR_CRAFTING_RECIPES.put(tool, recipe);
            else if (Reference.PLOWS.contains(tool))
                Reference.PLOW_CRAFTING_RECIPES.put(tool, recipe);
        }
    }

    // =========================================================
    // DEPENDENCIES & PERMISSIONS
    // =========================================================
    private void loadDependencies() {
        debuggingMessages.sendConsoleMessage(true, ChatColor.YELLOW + "Checking dependencies...");

        try {
            Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
            if (wg != null && wg.isEnabled() && wg instanceof WorldGuardPlugin) {
                worldguard = (WorldGuardPlugin) wg;
                debuggingMessages.sendConsoleMessage(true, ChatColor.GREEN + "WorldGuard plugin found and hooked.");
            } else if (isWorldGuardAvailable()) {
                debuggingMessages.sendConsoleMessage(true, ChatColor.GREEN + "WorldGuard API detected via WorldGuard.getInstance().");
            } else {
                worldguard = null;
                getLogger().warning("WorldGuard not found; continuing without region protection.");
            }
        } catch (Throwable t) {
            worldguard = null;
            getLogger().warning("WorldGuard not installed or inaccessible; region features disabled.");
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

    protected void generatePermission(List<String> powerToolNames, List<Material> materials) {
        for (int i = 0; i < powerToolNames.size() && i < materials.size(); i++) {
            String tool = powerToolNames.get(i);
            String type = tool.substring(tool.indexOf("_") + 1).toLowerCase();
            String material = tool.substring(0, tool.indexOf("_")).toLowerCase();

            String craft = "powermining.craft." + type + "." + material;
            String use = "powermining.use." + type + "." + material;
            String enchant = "powermining.enchant." + type + "." + material;

            Material item = materials.get(i);
            Reference.CRAFT_PERMISSIONS.put(item, craft);
            Reference.USE_PERMISSIONS.put(item, use);
            Reference.ENCHANT_PERMISSIONS.put(item, enchant);
        }
    }

    // =========================================================
    // GETTERS
    // =========================================================
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

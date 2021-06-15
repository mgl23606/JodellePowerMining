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

import com.palmergames.bukkit.towny.Towny;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jodelle.powermining.enchantment.Glow;
import jodelle.powermining.handlers.*;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PowerMining extends JavaPlugin {
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
    private Plugin griefprevention;
    private Plugin towny;

    private static PowerMining instance;

    @Override
    public void onEnable() {

        instance = this;

        debuggingMessages = new DebuggingMessages();

        this.saveDefaultConfig();
        registerGlow();
        processConfig();
        processCraftingRecipes();
        processPermissions();
        getLogger().info("Finished processing config file.");
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


        getLogger().info("PowerMining plugin was enabled.");


    }

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
        //Hashmap to store the permission
        // WOODEN_PICKAXE -> powermining.craft.hammer.wooden
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
     * @param powerToolNames Array containing the names of all the PowerTools
     * @param items List of the items
     */
    protected void generatePermission(@Nonnull final ArrayList<String> powerToolNames, @Nonnull final ArrayList<Material> items) {

        int i = 0;
        for (String tool : powerToolNames) {
            String craftPermission = "powermining.craft." + tool.substring(tool.indexOf("_") + 1).toLowerCase() + "." + tool.substring(0, tool.indexOf("_")).toLowerCase();
            String usePermission = "powermining.use." + tool.substring(tool.indexOf("_") + 1).toLowerCase() + "." + tool.substring(0, tool.indexOf("_")).toLowerCase();
            String enchantPermission = "powermining.enchant." + tool.substring(tool.indexOf("_") + 1).toLowerCase() + "." + tool.substring(0, tool.indexOf("_")).toLowerCase();

            Reference.CRAFT_PERMISSIONS.put(items.get(i), craftPermission);
            Reference.USE_PERMISSIONS.put(items.get(i), usePermission);
            Reference.ENCHANT_PERMISSIONS.put(items.get(i), enchantPermission);
             i++;
        }

    }

    /**
     * Reads the config file, processes each recipe and stores it on its respective HashMap
     */
    private void processCraftingRecipes() {
        boolean showDebugMessage = false;
        //This hashmap is used to store all the information about the recipe
        //The key is the name of the item, ex DIAMOND_HAMMER
        //The value is an array of materials where each position refers to the crafting table matrix
       // HashMap<String, ItemStack[]> craftingRecipes = new HashMap<>();
        // We start by getting the section recipes from the config file
        // Each element iterated is the name of the powertool, ex: POWER_HAMMER
        for (Object x : (ArrayList<?>) getConfig().getList("Recipes")) {
            // This HashMap contains all the names of the blocks of the recipe
            // as well as their quantities. Ex: DIAMOND*1
            LinkedHashMap<String, ArrayList> l = (LinkedHashMap<String, ArrayList>) x;
            for (String toolName : l.keySet()) {
                debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.BLUE + "Processing " + toolName + " recipe");
                // This array is used to store all 9 itemstacks used in the recipe
                // When an element is null signifies an empty slot in the crafting table
                ItemStack[] craftingRecipe = new ItemStack[9];
                int i = 0;
                for (String material : (ArrayList<String>) l.get(toolName)) {
                    //console.sendMessage(ChatColor.AQUA + hammerType);
                    // EMPTY means that the slot is empty, obviously
                    if (material.equals("EMPTY")) {
                        craftingRecipe[i] = null;
                        i++;
                        continue;
                    }
                    // The material and the quantity are separated by '*'
                    int separator = material.indexOf('*');
                    Material materialName = Material.getMaterial(material.substring(0, separator));

                    int quantity = Integer.parseInt(material.substring(separator + 1, material.length()));
                    if (quantity > 64) {

                    }
                    ItemStack itemStack = new ItemStack(materialName, quantity);
                    if (quantity > itemStack.getMaxStackSize()){
                        throw new NumberFormatException("A full stack of " + material + " can only contain " + itemStack.getMaxStackSize());
                    }
                    craftingRecipe[i] = itemStack;
                    debuggingMessages.sendConsoleMessage(showDebugMessage,ChatColor.GOLD + "Material: " + material);
                    debuggingMessages.sendConsoleMessage(showDebugMessage,ChatColor.GOLD + "Max stack size: " + String.valueOf(craftingRecipe[i].getMaxStackSize()));
                    debuggingMessages.sendConsoleMessage(showDebugMessage,ChatColor.GOLD + "ConfigFile stack size: " + quantity);
                    i++;
                }
                if (Reference.HAMMERS.contains(toolName)) {
                    Reference.HAMMER_CRAFTING_RECIPES.put(toolName, craftingRecipe);
                    debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.RED + toolName + " recipe processed successfully");
                    continue;
                }
                if (Reference.EXCAVATORS.contains(toolName)) {
                    Reference.EXCAVATOR_CRAFTING_RECIPES.put(toolName, craftingRecipe);
                    debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.RED + toolName + " recipe processed successfully");
                    continue;
                }
                if (Reference.PLOWS.contains(toolName)) {
                    Reference.PLOW_CRAFTING_RECIPES.put(toolName, craftingRecipe);
                    debuggingMessages.sendConsoleMessage(showDebugMessage, ChatColor.RED + toolName + " recipe processed successfully");
                    continue;
                }
            }

        }


        //console.sendMessage(ChatColor.AQUA + Integer.toString(Reference.EXCAVATOR_CRAFTING_RECIPES.size()));

        //Add the craftingRecipes hashmap to the Reference, so it can be accessed globally
        //Reference.CRAFTING_RECIPES = craftingRecipes;

    }

    @Override
    public void onDisable() {
        getLogger().info("PowerMining plugin was disabled.");
    }

    /**
     * Reads the config file and processes it
     */
    public void processConfig() {
        try {
            for (Object x : (ArrayList<?>) getConfig().getList("Minable")) {
                LinkedHashMap<String, ArrayList> l = (LinkedHashMap<String, ArrayList>) x;

                for (String blockType : l.keySet()) {
                    if (blockType == null || blockType.isEmpty())
                        continue;

                    if (Material.getMaterial(blockType) == null || Reference.MINABLE.containsKey(Material.getMaterial(blockType)))
                        continue;

                    Reference.MINABLE.put(Material.getMaterial(blockType), new ArrayList<Material>());
                    ArrayList<Material> temp = Reference.MINABLE.get(Material.getMaterial(blockType));

                    for (String hammerType : (ArrayList<String>) l.get(blockType)) {
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
            getLogger().info("NPE when trying to read the Minable list from the config file, check if it's set correctly!");
        }

        try {
            for (String blockType : getConfig().getStringList("Diggable")) {
                if (blockType == null || blockType.isEmpty())
                    continue;

                if (Material.getMaterial(blockType) != null && !Reference.DIGGABLE.contains(Material.getMaterial(blockType)))
                    Reference.DIGGABLE.add(Material.getMaterial(blockType));
            }
        } catch (NullPointerException e) {
            getLogger().info("NPE when trying to read the Digable list from the config file, check if it's set correctly!");
        }

        //Register for tools
        try {
            Reference.RADIUS = getConfig().getInt("Radius");
            Reference.DEEP = getConfig().getInt("Deep");
        } catch (NullPointerException e) {
            getLogger().info("HOE, check if hoes radius is currectly added.");
        }
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

    public GriefPrevention getGriefPrevention() {
        return (GriefPrevention) griefprevention;
    }

    public Towny getTowny() {
        return (Towny) towny;
    }

    public DebuggingMessages getDebuggingMessages() {
        return debuggingMessages;
    }

    public void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Glow glow = new Glow(new NamespacedKey(plugin, "glow"));
            Enchantment.registerEnchantment(glow);
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PowerMining getInstance() {
        return instance;
    }
}

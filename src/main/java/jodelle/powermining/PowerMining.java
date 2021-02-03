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
import jodelle.powermining.lib.Reference;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

public final class PowerMining extends JavaPlugin {
	public JavaPlugin plugin;
	PlayerInteractHandler handlerPlayerInteract;
	BlockBreakHandler handlerBlockBreak;
	CraftItemHandler handlerCraftItem;
	EnchantItemHandler handlerEnchantItem;
	InventoryClickHandler handlerInventoryClick;
	ClickPlayerHandler handlerClickPlayer;

	Plugin worldguard;
	Plugin griefprevention;
	Plugin towny;

    private static PowerMining instance;

    @Override
	public void onEnable(){

		instance = this;

		this.saveDefaultConfig();
		registerGlow();
		processConfig();
		processCraftingRecipes();
		getLogger().info("Finished processing config file.");


		handlerPlayerInteract = new PlayerInteractHandler();
		handlerBlockBreak = new BlockBreakHandler();
		handlerCraftItem = new CraftItemHandler();
		handlerEnchantItem = new EnchantItemHandler();
		handlerInventoryClick = new InventoryClickHandler();
		handlerClickPlayer = new ClickPlayerHandler();

		handlerPlayerInteract.Init(this);
		handlerBlockBreak.Init(this);
		handlerCraftItem.Init(this);
		handlerEnchantItem.Init(this);
		handlerInventoryClick.Init(this);
		handlerClickPlayer.Init(this);

		worldguard = getServer().getPluginManager().getPlugin("WorldGuard");
		griefprevention = getServer().getPluginManager().getPlugin("GriefPrevention");
		towny = getServer().getPluginManager().getPlugin("Towny");



		getLogger().info("PowerMining plugin was enabled.");






	}

	private void processCraftingRecipes() {
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

		//This hashmap is used to store all the information about the recipe
		//The key is the name of the item, ex DIAMOND_HAMMER
		//The value is an array of materials where each position refers to the crafting table matrix
		HashMap<String, ItemStack[]> craftingRecipes = new HashMap<>();

		// We start by getting the section recipes from the config file
		// Each element iterated is the name of the powertool, ex: POWER_HAMMER
		for (Object x : (ArrayList<?>) getConfig().getList("Recipes")) {
			try {

				// This HashMap contains all the names of the blocks of the recipe
				// as well as their quantities. Ex: DIAMOND*1
				LinkedHashMap<String, ArrayList> l = (LinkedHashMap<String, ArrayList>) x;
				for (String toolName : l.keySet()) {
					//console.sendMessage(ChatColor.RED + toolName);
					// This array is used to store all 9 itemstacks used in the recipe
					// When an element is null signifies an empty slot in the crafting table
					ItemStack[] craftingRecipe = new ItemStack[9];
					int i=0;
					for (String material: (ArrayList<String>)l.get(toolName)) {
						//console.sendMessage(ChatColor.AQUA + hammerType);
						// EMPTY means that the slot is empty, obviously
						if (material.equals("EMPTY")){
							craftingRecipe[i] = null;
							i++;
							continue;
						}
						// The material and the quantity are separated by '*'
						int separator = material.indexOf('*');
						Material materialName = Material.getMaterial(material.substring(0, separator));

						int quantity = Integer.parseInt(material.substring(separator+1, material.length()));
						if (quantity > 64){
							throw new NumberFormatException("A full stack can contain a maximum of 64 items");
						}

						craftingRecipe[i] = new ItemStack(materialName, quantity);
						i++;
					}
					if (Reference.HAMMERS.contains(toolName)) {
						//craftingRecipes.put(toolName, craftingRecipe);
						console.sendMessage(ChatColor.RED + "Found: " + toolName);
						Reference.HAMMER_CRAFTING_RECIPES.put(toolName, craftingRecipe);
					}else if(Reference.EXCAVATORS.contains(toolName)){
						console.sendMessage(ChatColor.RED + "Found: " + toolName);
						Reference.EXCAVATOR_CRAFTING_RECIPES.put(toolName, craftingRecipe);
					}else if(Reference.PLOWS.contains(toolName)){
						console.sendMessage(ChatColor.RED + "Found: " + toolName);
						Reference.PLOW_CRAFTING_RECIPES.put(toolName, craftingRecipe);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				getLogger().info("Error loading config file: " + e.getMessage());
			}
		}



		console.sendMessage(ChatColor.AQUA + Integer.toString(Reference.EXCAVATOR_CRAFTING_RECIPES.size()));

//		for (ItemStack[] itemStacks : craftingRecipes.values()) {
//			console.sendMessage(ChatColor.RED + "Item");
//			for (ItemStack stack : itemStacks) {
//				if (stack != null) {
//					console.sendMessage(ChatColor.AQUA + stack.getType().toString() + ": " + stack.getAmount());
//				}else{
//					console.sendMessage(ChatColor.AQUA + "Null");
//				}
//			}
//		}

		//Add the craftingRecipes hashmap to the Reference, so it can be accessed globally
		Reference.CRAFTING_RECIPES = craftingRecipes;

	}

	@Override
	public void onDisable() {
		getLogger().info("PowerMining plugin was disabled.");
	}


	public void processConfig() {
		try {
			for (Object x : (ArrayList<?>) getConfig().getList("Minable")) {
				LinkedHashMap<String, ArrayList> l = (LinkedHashMap<String, ArrayList>)x;

				for (String blockType: l.keySet()) {
					if (blockType == null || blockType.isEmpty())
						continue;

					if (Material.getMaterial(blockType) == null || Reference.MINABLE.containsKey(Material.getMaterial(blockType)))
						continue;

					Reference.MINABLE.put(Material.getMaterial(blockType), new ArrayList<Material>());
					ArrayList<Material> temp = Reference.MINABLE.get(Material.getMaterial(blockType));

					for (String hammerType: (ArrayList<String>)l.get(blockType)) {
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
		}
		catch (NullPointerException e) {
			getLogger().info("NPE when trying to read the Minable list from the config file, check if it's set correctly!");
		}

		try {
			for (String blockType : getConfig().getStringList("Diggable")) {
				if (blockType == null || blockType.isEmpty())
					continue;

				if (Material.getMaterial(blockType) != null && ! Reference.DIGGABLE.contains(Material.getMaterial(blockType)))
					Reference.DIGGABLE.add(Material.getMaterial(blockType));
			}
		}
		catch (NullPointerException e) {
			getLogger().info("NPE when trying to read the Digable list from the config file, check if it's set correctly!");
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

	public void registerGlow() {
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Glow glow = new Glow(new NamespacedKey(plugin, "glow"));
			Enchantment.registerEnchantment(glow);
		}
		catch (IllegalArgumentException e){
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

    public static PowerMining getInstance() {
        return instance;
    }
}

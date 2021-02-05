/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for cancelling the crafting in case the user does not have permission
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CraftItemListener implements Listener {
	PowerMining plugin;
	ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	ItemStack[] newMatrix;
	public CraftItemListener(PowerMining plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	// This method checks if everything is ok when a player is crafting
	// Because theres no way to set the amount of each item in the shaped recipe
	// One option is to check the quantity while he is crafting
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void canCraft(CraftItemEvent event) {
		ItemStack resultItem = event.getRecipe().getResult();

		// Check if the item is a power tool
		if (!PowerUtils.isPowerTool(resultItem)) {
			return;
		}

		// Check if the player has crafting permission for this item type
		if (!PowerUtils.checkCraftPermission((Player) event.getWhoClicked(), resultItem.getType())) {
			event.setCancelled(true);
		}

		console.sendMessage(ChatColor.RED + "O gajo ta a craftar uma Powertool...");

		// Get the name of the powertool stored in the persistentdatacontainer
		PersistentDataContainer container = resultItem.getItemMeta().getPersistentDataContainer();
		NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");
		String powerToolName = container.get(isPowerTool, PersistentDataType.STRING);
		console.sendMessage(ChatColor.RED + "You're crafting a: " + powerToolName);

		CraftingInventory inventory = event.getInventory();
		ItemStack[] matrix = inventory.getMatrix();

		boolean isRecipeOk = false;
		newMatrix = new ItemStack[matrix.length];
		// We start by searching the powertool in the Arraylists
		// After finding it, we can check the correspondent HashMap which contains the recipes
		if (Reference.HAMMERS.contains(powerToolName)){
			ItemStack[] expectedRecipe = Reference.HAMMER_CRAFTING_RECIPES.get(powerToolName);
			isRecipeOk = checkCraftingMatrix(inventory, matrix, expectedRecipe);
		}else if(Reference.EXCAVATORS.contains(powerToolName)){
			ItemStack[] expectedRecipe = Reference.EXCAVATOR_CRAFTING_RECIPES.get(powerToolName);
			isRecipeOk = checkCraftingMatrix(inventory, matrix, expectedRecipe);
		}else if(Reference.PLOWS.contains(powerToolName)){
			ItemStack[] expectedRecipe = Reference.PLOW_CRAFTING_RECIPES.get(powerToolName);
			isRecipeOk = checkCraftingMatrix(inventory, matrix, expectedRecipe);
		}

		// If the recipe is not ok, the player can't take the item out of the crafted slot
		if (!isRecipeOk){
			event.setCancelled(true);
			return;
		}

		// If everything is ok, we clean the crafting matrix
		// This is neede because when we take the item, it only removes 1 of each
		// from the crafting table.
		event.getInventory().setMatrix(newMatrix);
	}

//	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
//	public void checkRecipe(PrepareItemCraftEvent event){
//		PowerMining plugin = PowerMining.getInstance();
//
//		Recipe recipe = event.getRecipe();
//
//		if (recipe == null){
//			return;
//		}
//		console.sendMessage(ChatColor.RED + "O gajo ta a craftar...");
//		ItemStack result = recipe.getResult();
//
//		if (!PowerUtils.isPowerTool(result)){
//			return;
//		}
//		console.sendMessage(ChatColor.RED + "O gajo ta a craftar uma Powertool...");
//
//
//		PersistentDataContainer container = result.getItemMeta().getPersistentDataContainer();
//		NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");
//
//		String powerToolName = container.get(isPowerTool, PersistentDataType.STRING);
//
//
//		console.sendMessage(ChatColor.RED + "You're crafting a: " + powerToolName);
//
//		CraftingInventory inventory = event.getInventory();
//		ItemStack[] matrix = inventory.getMatrix();
//		if (Reference.HAMMERS.contains(powerToolName)){
//			ItemStack[] expectedRecipe = Reference.HAMMER_CRAFTING_RECIPES.get(powerToolName);
//			checkCraftingMatrix(inventory, matrix, expectedRecipe);
//		}else if(Reference.EXCAVATORS.contains(powerToolName)){
//			ItemStack[] expectedRecipe = Reference.EXCAVATOR_CRAFTING_RECIPES.get(powerToolName);
//			checkCraftingMatrix(inventory, matrix, expectedRecipe);
//		}else if(Reference.PLOWS.contains(powerToolName)){
//			ItemStack[] expectedRecipe = Reference.PLOW_CRAFTING_RECIPES.get(powerToolName);
//			checkCraftingMatrix(inventory, matrix, expectedRecipe);
//		}
//
//
//
//
//	}

	private boolean checkCraftingMatrix(CraftingInventory inventory, ItemStack[] matrix, ItemStack[] expectedRecipe) {

		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] != null && expectedRecipe[i] != null){
				if (matrix[i].getAmount() < expectedRecipe[i].getAmount()){
					console.sendMessage(ChatColor.RED + "Nao meteste " + expectedRecipe[i].getType().toString() + " que chegue");
					//inventory.setResult(null);
					return false;
				}
				newMatrix[i] = matrix[i].clone();
				newMatrix[i].setAmount(matrix[i].getAmount() - expectedRecipe[i].getAmount());
			}
		}
		return true;
	}
}

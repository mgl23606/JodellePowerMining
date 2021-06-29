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
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CraftItemListener implements Listener {
	private final PowerMining plugin;
	private final DebuggingMessages debuggingMessages;
	private final boolean debugging = true;

	public CraftItemListener(@Nonnull final PowerMining plugin) {
		this.plugin = plugin;
		debuggingMessages = plugin.getDebuggingMessages();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	// This method checks if everything is ok when a player is crafting
	// Because theres no way to set the amount of each item in the shaped recipe
	// One option is to check the quantity while he is crafting
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void canCraft(CraftItemEvent event) {
		final HumanEntity whoClicked = event.getWhoClicked();
		final ItemStack resultItem = event.getRecipe().getResult();
		final ItemMeta itemMeta = resultItem.getItemMeta();

		if (basicVerifications(event, resultItem, itemMeta)){
			debuggingMessages.sendConsoleMessage(debugging, ChatColor.BLUE+"Verifications not ok");
			return;
		}

		// Get the name of the powertool stored in the persistentdatacontainer
		final String powerToolName = getPowerToolName(itemMeta);

		final CraftingInventory inventory = event.getInventory();
		final ItemStack[] matrix = inventory.getMatrix();

		//First we get the expected recipe so we can compare it with the current recipe
		final ItemStack[] expectedRecipe = getExpectedRecipe(powerToolName);

		// If the recipe is not ok, the player can't take the item out of the crafted slot
		if (!checkCraftingMatrix(matrix, expectedRecipe, whoClicked)){
			debuggingMessages.sendConsoleMessage(debugging, ChatColor.BLUE+"Recipe not ok");
			event.setCancelled(true);
			return;
		}

		// If everything is ok, we change crafting matrix amounts
		// This is needed because when we take the item, it only removes 1 of each
		// from the crafting table.
		// Also checks if the item on the slot has some kind of enchantments and passes
		// them to the result item upon crafting.
		updateCraftingMatrix(inventory, matrix, expectedRecipe);


	}

	/**
	 * Update the item amount on the crafting table. Also add the enchantments present
	 * on the used items to the new PowerTool
	 * @param inventory Inventory of the player
	 * @param matrix Matrix of the crafting table
	 * @param expectedRecipe Matrix of the expected recipe
	 */
	private void updateCraftingMatrix(@Nonnull final CraftingInventory inventory, @Nonnull final ItemStack[] matrix, @Nonnull final ItemStack[] expectedRecipe) {
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] != null && expectedRecipe[i] != null){
				matrix[i].setAmount(matrix[i].getAmount() - expectedRecipe[i].getAmount()+1);
				Map<Enchantment, Integer> enchantments = matrix[i].getEnchantments();
				ItemStack result = inventory.getResult();
				if (result != null){
					result.addEnchantments(enchantments);
				}
			}
		}
	}

	@NotNull
	private ItemStack[] getExpectedRecipe(@Nonnull final String powerToolName){
		ItemStack[] expectedRecipe = null;

		if (Reference.HAMMERS.contains(powerToolName)){
			expectedRecipe = Reference.HAMMER_CRAFTING_RECIPES.get(powerToolName);
		}else if(Reference.EXCAVATORS.contains(powerToolName)){
			expectedRecipe = Reference.EXCAVATOR_CRAFTING_RECIPES.get(powerToolName);
		}else if(Reference.PLOWS.contains(powerToolName)){
			expectedRecipe = Reference.PLOW_CRAFTING_RECIPES.get(powerToolName);
		}

		Validate.notNull(expectedRecipe);

		return expectedRecipe;
	}

	/**
	 * Accesses the PersistantDataContainer of the item and gets the name of the PowerTool
	 * @param itemMeta Meta of the item
	 * @return Returns the name of the PowerTool
	 */
	@NotNull
	private String getPowerToolName(@Nonnull ItemMeta itemMeta) {
		PersistentDataContainer container = itemMeta.getPersistentDataContainer();
		NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");
		String powerToolName = container.get(isPowerTool, PersistentDataType.STRING);

		Validate.notNull(powerToolName);

		return powerToolName;
	}

	private boolean basicVerifications(@Nonnull CraftItemEvent event, @Nonnull ItemStack resultItem, @Nullable ItemMeta itemMeta) {
		// Check if the item is a power tool
		if (!PowerUtils.isPowerTool(resultItem)) {
			debuggingMessages.sendConsoleMessage(debugging, ChatColor.BLUE + "The item is not a PowerTool.");
			return true;
		}

		// Check if the player has crafting permission for this item type
		if (!PowerUtils.checkCraftPermission((Player) event.getWhoClicked(), resultItem.getType())) {
			debuggingMessages.sendConsoleMessage(debugging, ChatColor.BLUE + "The player doesn't have permissions");
			event.setCancelled(true);
		}

		Validate.notNull(itemMeta);

		return false;
	}

	/**
	 * Checks the crafting recipe and item amounts
	 * @param matrix Crafting table matrix
	 * @param expectedRecipe Expected recipe matrix
	 * @param whoClicked Player who is crafting
	 * @return True if the recipe and its amounts are correct
	 */
	private boolean checkCraftingMatrix(@Nonnull final ItemStack[] matrix, @Nonnull final ItemStack[] expectedRecipe, @Nonnull final HumanEntity whoClicked) {
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] != null && expectedRecipe[i] != null){
				if (matrix[i].getAmount() < expectedRecipe[i].getAmount()){
					debuggingMessages.sendConsoleMessage(debugging, ChatColor.RED + "You didn't add enough" + expectedRecipe[i].getType());
					whoClicked.sendMessage(ChatColor.RED + "[JodellePowerMining] - You didn't add enough " + expectedRecipe[i].getType());
					//inventory.setResult(null);
					return false;
				}
			}
		}
		return true;
	}
}

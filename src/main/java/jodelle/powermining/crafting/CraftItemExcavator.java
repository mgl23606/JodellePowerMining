/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for creating the Excavator items and their respective crafting recipes
 */

package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.enchantment.Glow;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CraftItemExcavator {
	public JavaPlugin plugin;
	public static String loreString = "POUND!";

	ItemStack WoodExcavator = new ItemStack(Material.WOODEN_SHOVEL, 1);
	ItemStack StoneExcavator = new ItemStack(Material.STONE_SHOVEL, 1);
	ItemStack IronExcavator = new ItemStack(Material.IRON_SHOVEL, 1);
	ItemStack GoldExcavator = new ItemStack(Material.GOLDEN_SHOVEL, 1);
	ItemStack DiamondExcavator = new ItemStack(Material.DIAMOND_SHOVEL, 1);
	ItemStack NetheriteExcavator = new ItemStack(Material.NETHERITE_SHOVEL, 1);

	ShapedRecipe WoodExcavatorRecipe;
	ShapedRecipe WoodExcavatorBirchRecipe;
	ShapedRecipe WoodExcavatorAcaciaRecipe;
	ShapedRecipe WoodExcavatorJungleRecipe;
	ShapedRecipe WoodExcavatorDarkOakRecipe;
	ShapedRecipe WoodExcavatorSpurceRecipe;

	ShapedRecipe StoneExcavatorRecipe;
	ShapedRecipe IronExcavatorRecipe;
	ShapedRecipe GoldExcavatorRecipe;
	ShapedRecipe DiamondExcavatorRecipe;
	ShapedRecipe NetheriteExcavatorRecipe;

	public CraftItemExcavator(JavaPlugin plugin) {
		this.plugin = plugin;

		modifyItemMeta();
		setRecipes();
		registerRecipes();
	}

	// Get metadata for all excavator types, add lore and change the names to identify them as excavators
	public void modifyItemMeta() {

		ItemMeta WoodExcavatorMeta = WoodExcavator.getItemMeta();
		ItemMeta StoneExcavatorMeta = StoneExcavator.getItemMeta();
		ItemMeta IronExcavatorMeta = IronExcavator.getItemMeta();
		ItemMeta GoldExcavatorMeta = GoldExcavator.getItemMeta();
		ItemMeta DiamondExcavatorMeta = DiamondExcavator.getItemMeta();
		ItemMeta NetheriteExcavatorMeta = NetheriteExcavator.getItemMeta();

		NamespacedKey isPowerTool = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
		WoodExcavatorMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
		StoneExcavatorMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
		IronExcavatorMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
		GoldExcavatorMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
		DiamondExcavatorMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
		NetheriteExcavatorMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);

		ArrayList<String> lore = new ArrayList<String>();
		lore.add(loreString);

		WoodExcavatorMeta.setDisplayName("Wooden Excavator");
		StoneExcavatorMeta.setDisplayName("Stone Excavator");
		IronExcavatorMeta.setDisplayName("Iron Excavator");
		GoldExcavatorMeta.setDisplayName("Golden Excavator");
		DiamondExcavatorMeta.setDisplayName("Diamond Excavator");
		NetheriteExcavatorMeta.setDisplayName("Netherite Excavator");

		WoodExcavatorMeta.setLore(lore);
		WoodExcavatorMeta.addEnchant(new Glow(new NamespacedKey(plugin,"WoodExcavator")),  1, false);

		StoneExcavatorMeta.setLore(lore);
		StoneExcavatorMeta.addEnchant(new Glow(new NamespacedKey(plugin,"StoneExcavator")),  1, false);

		IronExcavatorMeta.setLore(lore);
		IronExcavatorMeta.addEnchant(new Glow(new NamespacedKey(plugin,"IronExcavator")),  1, false);

		GoldExcavatorMeta.setLore(lore);
		GoldExcavatorMeta.addEnchant(new Glow(new NamespacedKey(plugin,"GoldExcavator")),  1, false);

		DiamondExcavatorMeta.setLore(lore);
		DiamondExcavatorMeta.addEnchant(new Glow(new NamespacedKey(plugin,"DiamondExcavator")),  1, false);

		NetheriteExcavatorMeta.setLore(lore);
		NetheriteExcavatorMeta.addEnchant(new Glow(new NamespacedKey(plugin,"NetheriteExcavator")),  1, false);

		WoodExcavator.setItemMeta(WoodExcavatorMeta);
		StoneExcavator.setItemMeta(StoneExcavatorMeta);
		IronExcavator.setItemMeta(IronExcavatorMeta);
		GoldExcavator.setItemMeta(GoldExcavatorMeta);
		DiamondExcavator.setItemMeta(DiamondExcavatorMeta);
		NetheriteExcavator.setItemMeta(NetheriteExcavatorMeta);
	}

	// Creates the ShapedRecipe patterns for all excavator types
	public void setRecipes() {
		WoodExcavatorRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodExcavator"),WoodExcavator);
		WoodExcavatorBirchRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodExcavatorBirchRecipe"),WoodExcavator);
		WoodExcavatorAcaciaRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodExcavatorAcaciaRecipe"),WoodExcavator);
		WoodExcavatorJungleRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodExcavatorJungleRecipe"),WoodExcavator);
		WoodExcavatorDarkOakRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodExcavatorDarkOakRecipe"),WoodExcavator);
		WoodExcavatorSpurceRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodExcavatorSpurceRecipe"),WoodExcavator);
		StoneExcavatorRecipe = new ShapedRecipe(new NamespacedKey(plugin,"StoneExcavator"), StoneExcavator);
		IronExcavatorRecipe = new ShapedRecipe(new NamespacedKey(plugin,"IronExcavator"),IronExcavator);
		GoldExcavatorRecipe = new ShapedRecipe(new NamespacedKey(plugin,"GoldExcavator"),GoldExcavator);
		DiamondExcavatorRecipe = new ShapedRecipe(new NamespacedKey(plugin,"DiamondExcavator"),DiamondExcavator);
		NetheriteExcavatorRecipe = new ShapedRecipe(new NamespacedKey(plugin,"NetheriteExcavator"),NetheriteExcavator);

		WoodExcavatorRecipe.shape(" m ", "mim", " m ");
		WoodExcavatorRecipe.setIngredient('m', Material.OAK_LOG);
		WoodExcavatorRecipe.setIngredient('i', Material.WOODEN_SHOVEL);

		WoodExcavatorBirchRecipe.shape(" m ", "mim", " m ");
		WoodExcavatorBirchRecipe.setIngredient('m', Material.BIRCH_LOG);
		WoodExcavatorBirchRecipe.setIngredient('i', Material.WOODEN_SHOVEL);

		WoodExcavatorAcaciaRecipe.shape(" m ", "mim", " m ");
		WoodExcavatorAcaciaRecipe.setIngredient('m', Material.ACACIA_LOG);
		WoodExcavatorAcaciaRecipe.setIngredient('i', Material.WOODEN_SHOVEL);

		WoodExcavatorJungleRecipe.shape(" m ", "mim", " m ");
		WoodExcavatorJungleRecipe.setIngredient('m', Material.JUNGLE_LOG);
		WoodExcavatorJungleRecipe.setIngredient('i', Material.WOODEN_SHOVEL);

		WoodExcavatorDarkOakRecipe.shape(" m ", "mim", " m ");
		WoodExcavatorDarkOakRecipe.setIngredient('m', Material.DARK_OAK_LOG);
		WoodExcavatorDarkOakRecipe.setIngredient('i', Material.WOODEN_SHOVEL);

		WoodExcavatorSpurceRecipe.shape(" m ", "mim", " m ");
		WoodExcavatorSpurceRecipe.setIngredient('m', Material.SPRUCE_LOG);
		WoodExcavatorSpurceRecipe.setIngredient('i', Material.WOODEN_SHOVEL);

		StoneExcavatorRecipe.shape(" m ", "mim", " m ");
		StoneExcavatorRecipe.setIngredient('m', Material.STONE);
		StoneExcavatorRecipe.setIngredient('i', Material.STONE_SHOVEL);

		IronExcavatorRecipe.shape(" m ", "mim", " m ");
		IronExcavatorRecipe.setIngredient('m', Material.IRON_INGOT);
		IronExcavatorRecipe.setIngredient('i', Material.IRON_SHOVEL);

		GoldExcavatorRecipe.shape(" m ", "mim", " m ");
		GoldExcavatorRecipe.setIngredient('m', Material.GOLD_INGOT);
		GoldExcavatorRecipe.setIngredient('i', Material.GOLDEN_SHOVEL);

		DiamondExcavatorRecipe.shape(" m ", "mim", " m ");
		DiamondExcavatorRecipe.setIngredient('m', Material.DIAMOND);
		DiamondExcavatorRecipe.setIngredient('i', Material.DIAMOND_SHOVEL);

		NetheriteExcavatorRecipe.shape(" m ", "mim", " m ");
		NetheriteExcavatorRecipe.setIngredient('m', Material.NETHERITE_INGOT);
		NetheriteExcavatorRecipe.setIngredient('i', Material.NETHERITE_SHOVEL);
	}

	// Registers all created recipes into the game
	public void registerRecipes() {
		plugin.getServer().addRecipe(WoodExcavatorRecipe);
		plugin.getServer().addRecipe(WoodExcavatorBirchRecipe);
		plugin.getServer().addRecipe(WoodExcavatorAcaciaRecipe);
		plugin.getServer().addRecipe(WoodExcavatorJungleRecipe);
		plugin.getServer().addRecipe(WoodExcavatorDarkOakRecipe);
		plugin.getServer().addRecipe(WoodExcavatorSpurceRecipe);
		plugin.getServer().addRecipe(StoneExcavatorRecipe);
		plugin.getServer().addRecipe(IronExcavatorRecipe);
		plugin.getServer().addRecipe(GoldExcavatorRecipe);
		plugin.getServer().addRecipe(DiamondExcavatorRecipe);
		plugin.getServer().addRecipe(NetheriteExcavatorRecipe);
	}
}

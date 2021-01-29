/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for creating the Hammer items and their respective crafting recipes
 */

package jodelle.powermining.crafting;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CraftItemHammer {
	public JavaPlugin plugin;
	public static String loreString = "SMASH!";

	ItemStack WoodHammer = new ItemStack(Material.WOODEN_PICKAXE, 1);
	ItemStack StoneHammer = new ItemStack(Material.STONE_PICKAXE, 1);
	ItemStack IronHammer = new ItemStack(Material.IRON_PICKAXE, 1);
	ItemStack GoldHammer = new ItemStack(Material.GOLDEN_PICKAXE, 1);
	ItemStack DiamondHammer = new ItemStack(Material.DIAMOND_PICKAXE, 1);
	ItemStack NetheriteHammer = new ItemStack(Material.NETHERITE_PICKAXE, 1);

	ShapedRecipe WoodHammerRecipe;
	ShapedRecipe WoodHammerOakRecipe;
	ShapedRecipe WoodHammerDarkOakRecipe;
	ShapedRecipe WoodHammerJungleRecipe;
	ShapedRecipe WoodHammerBirchRecipe;
	ShapedRecipe WoodHammerAcaciaRecipe;
	ShapedRecipe StoneHammerRecipe;
	ShapedRecipe IronHammerRecipe;
	ShapedRecipe GoldHammerRecipe;
	ShapedRecipe DiamondHammerRecipe;
	ShapedRecipe NetheriteHammerRecipe;

	public CraftItemHammer(JavaPlugin plugin) {
		this.plugin = plugin;

		modifyItemMeta();
		setRecipes();
		registerRecipes();
	}

	// Get metadata for all hammer types, add lore and change the names to identify them as hammers
	public void modifyItemMeta() {
		ItemMeta WoodHammerMeta = WoodHammer.getItemMeta();
		ItemMeta StoneHammerMeta = StoneHammer.getItemMeta();
		ItemMeta IronHammerMeta = IronHammer.getItemMeta();
		ItemMeta GoldHammerMeta = GoldHammer.getItemMeta();
		ItemMeta DiamondHammerMeta = DiamondHammer.getItemMeta();
		ItemMeta NetheriteHammerMeta = NetheriteHammer.getItemMeta();

		ArrayList<String> lore = new ArrayList<String>();
		lore.add(loreString);

		WoodHammerMeta.setDisplayName("Wooden Hammer");
		StoneHammerMeta.setDisplayName("Stone Hammer");
		IronHammerMeta.setDisplayName("Iron Hammer");
		GoldHammerMeta.setDisplayName("Golden Hammer");
		DiamondHammerMeta.setDisplayName("Diamond Hammer");
		NetheriteHammerMeta.setDisplayName("Netherite Hammer");

		WoodHammerMeta.setLore(lore);
		StoneHammerMeta.setLore(lore);
		IronHammerMeta.setLore(lore);
		GoldHammerMeta.setLore(lore);
		DiamondHammerMeta.setLore(lore);
		NetheriteHammerMeta.setLore(lore);

		WoodHammer.setItemMeta(WoodHammerMeta);
		StoneHammer.setItemMeta(StoneHammerMeta);
		IronHammer.setItemMeta(IronHammerMeta);
		GoldHammer.setItemMeta(GoldHammerMeta);
		DiamondHammer.setItemMeta(DiamondHammerMeta);
		NetheriteHammer.setItemMeta(NetheriteHammerMeta);
	}

	// Creates the ShapedRecipe patterns for all hammer types
	public void setRecipes() {
		WoodHammerRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodHammer"), WoodHammer);
		WoodHammerOakRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodOakHammer"), WoodHammer);
		WoodHammerDarkOakRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodDarkOakHammer"), WoodHammer);
		WoodHammerJungleRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodHammerJungle"), WoodHammer);
		WoodHammerBirchRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodHammerBirch"), WoodHammer);
		WoodHammerAcaciaRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodHammerAcacia"), WoodHammer);
		StoneHammerRecipe = new ShapedRecipe(new NamespacedKey(plugin, "StoneHammer"), StoneHammer);
		IronHammerRecipe = new ShapedRecipe(new NamespacedKey(plugin, "IronHammer"), IronHammer);
		GoldHammerRecipe = new ShapedRecipe(new NamespacedKey(plugin, "GoldHammer"), GoldHammer);
		DiamondHammerRecipe = new ShapedRecipe(new NamespacedKey(plugin, "DiamondHammer"), DiamondHammer);
		NetheriteHammerRecipe = new ShapedRecipe(new NamespacedKey(plugin, "NetheriteHammer"), NetheriteHammer);

		WoodHammerRecipe.shape(" m ", "mim", " m ");
		WoodHammerRecipe.setIngredient('m', Material.SPRUCE_LOG);
		WoodHammerRecipe.setIngredient('i', Material.WOODEN_PICKAXE);

		WoodHammerOakRecipe.shape(" m ", "mim", " m ");
		WoodHammerOakRecipe.setIngredient('m', Material.OAK_LOG);
		WoodHammerOakRecipe.setIngredient('i', Material.WOODEN_PICKAXE);

		WoodHammerDarkOakRecipe.shape(" m ", "mim", " m ");
		WoodHammerDarkOakRecipe.setIngredient('m', Material.DARK_OAK_LOG);
		WoodHammerDarkOakRecipe.setIngredient('i', Material.WOODEN_PICKAXE);

		WoodHammerJungleRecipe.shape(" m ", "mim", " m ");
		WoodHammerJungleRecipe.setIngredient('m', Material.JUNGLE_LOG);
		WoodHammerJungleRecipe.setIngredient('i', Material.WOODEN_PICKAXE);

		WoodHammerBirchRecipe.shape(" m ", "mim", " m ");
		WoodHammerBirchRecipe.setIngredient('m', Material.BIRCH_LOG);
		WoodHammerBirchRecipe.setIngredient('i', Material.WOODEN_PICKAXE);

		WoodHammerAcaciaRecipe.shape(" m ", "mim", " m ");
		WoodHammerAcaciaRecipe.setIngredient('m', Material.ACACIA_LOG);
		WoodHammerAcaciaRecipe.setIngredient('i', Material.WOODEN_PICKAXE);

		StoneHammerRecipe.shape(" m ", "mim", " m ");
		StoneHammerRecipe.setIngredient('m', Material.STONE);
		StoneHammerRecipe.setIngredient('i', Material.STONE_PICKAXE);

		IronHammerRecipe.shape(" m ", "mim", " m ");
		IronHammerRecipe.setIngredient('m', Material.IRON_INGOT);
		IronHammerRecipe.setIngredient('i', Material.IRON_PICKAXE);

		GoldHammerRecipe.shape(" m ", "mim", " m ");
		GoldHammerRecipe.setIngredient('m', Material.GOLD_INGOT);
		GoldHammerRecipe.setIngredient('i', Material.GOLDEN_PICKAXE);

		DiamondHammerRecipe.shape(" m ", "mim", " m ");
		DiamondHammerRecipe.setIngredient('m', Material.DIAMOND);
		DiamondHammerRecipe.setIngredient('i', Material.DIAMOND_PICKAXE);

		NetheriteHammerRecipe.shape(" m ", "mim", " m ");
		NetheriteHammerRecipe.setIngredient('m', Material.NETHERITE_INGOT);
		NetheriteHammerRecipe.setIngredient('i', Material.NETHERITE_PICKAXE);
	}

	// Registers all created recipes into the game
	public void registerRecipes() {
		plugin.getServer().addRecipe(WoodHammerRecipe);
		plugin.getServer().addRecipe(WoodHammerOakRecipe);
		plugin.getServer().addRecipe(WoodHammerDarkOakRecipe);
		plugin.getServer().addRecipe(WoodHammerJungleRecipe);
		plugin.getServer().addRecipe(WoodHammerBirchRecipe);
		plugin.getServer().addRecipe(WoodHammerAcaciaRecipe);
		plugin.getServer().addRecipe(StoneHammerRecipe);
		plugin.getServer().addRecipe(IronHammerRecipe);
		plugin.getServer().addRecipe(GoldHammerRecipe);
		plugin.getServer().addRecipe(DiamondHammerRecipe);
		plugin.getServer().addRecipe(NetheriteHammerRecipe);
	}

}

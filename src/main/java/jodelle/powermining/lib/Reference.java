/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Keeps a reference to several CONSTs used throughout the code
 */

package jodelle.powermining.lib;

import com.sk89q.worldguard.blacklist.target.MaterialTarget;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Reference {

	public static HashMap<String, ItemStack[]> CRAFTING_RECIPES = new HashMap<String, ItemStack[]>();
	public static HashMap<String, ItemStack[]> HAMMER_CRAFTING_RECIPES = new HashMap<String, ItemStack[]>();
	public static HashMap<String, ItemStack[]> EXCAVATOR_CRAFTING_RECIPES = new HashMap<String, ItemStack[]>();
	public static HashMap<String, ItemStack[]> PLOW_CRAFTING_RECIPES = new HashMap<String, ItemStack[]>();

	public static HashMap<Material, ArrayList<Material>> MINABLE = new HashMap<Material, ArrayList<Material>>();

	public static ArrayList<Material> DIGGABLE = new ArrayList<Material>();

	public static ArrayList<Material> TILLABLE = new ArrayList<Material>(Arrays.asList(
			Material.GRASS_BLOCK,
			Material.DIRT
	));

	public static ArrayList<Material> PATH = new ArrayList<Material>(Arrays.asList(
			Material.GRASS_BLOCK
	));

	public static ArrayList<Material> MINABLE_SILKTOUCH =  new ArrayList<Material>(Arrays.asList(
		Material.STONE,
		Material.COAL_ORE,
		Material.REDSTONE_ORE,
		Material.LAPIS_ORE,
		Material.DIAMOND_ORE,
		Material.ANCIENT_DEBRIS,
		Material.EMERALD_ORE,
		Material.ICE,
		Material.NETHER_QUARTZ_ORE,
		Material.GLOWSTONE,
		Material.ANCIENT_DEBRIS
	));

	public static ArrayList<Material> DIGGABLE_SILKTOUCH =  new ArrayList<Material>(Arrays.asList(
		Material.GRASS,
		Material.CLAY,
		Material.SNOW_BLOCK,
		Material.MYCELIUM,
		Material.GLOWSTONE
	));

	public static HashMap<Material, Material> MINABLE_FORTUNE;
	static {
		MINABLE_FORTUNE = new HashMap<Material, Material>();

		MINABLE_FORTUNE.put(Material.COAL_ORE, Material.COAL);
		MINABLE_FORTUNE.put(Material.REDSTONE_ORE, Material.REDSTONE);
		MINABLE_FORTUNE.put(Material.LAPIS_ORE, Material.LAPIS_LAZULI);
		MINABLE_FORTUNE.put(Material.DIAMOND_ORE, Material.DIAMOND);
		MINABLE_FORTUNE.put(Material.EMERALD_ORE, Material.EMERALD);
		MINABLE_FORTUNE.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
		MINABLE_FORTUNE.put(Material.GLOWSTONE, Material.GLOWSTONE_DUST);
	};

	public static HashMap<Material, Material> DIGGABLE_FORTUNE;
	static {
		DIGGABLE_FORTUNE = new HashMap<Material, Material>();

		DIGGABLE_FORTUNE.put(Material.GRAVEL, Material.FLINT);
		DIGGABLE_FORTUNE.put(Material.GLOWSTONE, Material.GLOWSTONE_DUST);
	};

	public static ArrayList<Material> PICKAXES =  new ArrayList<Material>(Arrays.asList(
		Material.WOODEN_PICKAXE,
		Material.STONE_PICKAXE,
		Material.IRON_PICKAXE,
		Material.GOLDEN_PICKAXE,
		Material.DIAMOND_PICKAXE,
		Material.NETHERITE_PICKAXE
	));

	public static ArrayList<Material> SHOVELS =  new ArrayList<Material>(Arrays.asList(
			Material.WOODEN_SHOVEL,
			Material.STONE_SHOVEL,
			Material.IRON_SHOVEL,
			Material.GOLDEN_SHOVEL,
			Material.DIAMOND_SHOVEL,
			Material.NETHERITE_SHOVEL
	));


	public static ArrayList<Material> SPADES = new ArrayList<Material>(Arrays.asList(
		Material.WOODEN_SHOVEL,
		Material.STONE_SHOVEL,
		Material.IRON_SHOVEL,
		Material.GOLDEN_SHOVEL,
		Material.DIAMOND_SHOVEL,
		Material.NETHERITE_SHOVEL
	));

	public static ArrayList<Material> HOES = new ArrayList<Material>(Arrays.asList(
			Material.WOODEN_HOE,
			Material.STONE_HOE,
			Material.IRON_HOE,
			Material.GOLDEN_HOE,
			Material.DIAMOND_HOE,
			Material.NETHERITE_HOE
	));

	public static Integer RADIUS;

	public static Integer DEEP;


	public static ArrayList<String> HAMMERS = new ArrayList<String>(Arrays.asList(
			 "WOODEN_HAMMER",
			 "STONE_HAMMER",
			 "IRON_HAMMER",
			 "GOLDEN_HAMMER",
			 "DIAMOND_HAMMER",
			 "NETHERITE_HAMMER"
	));

	public static ArrayList<String> EXCAVATORS = new ArrayList<String>(Arrays.asList(
			"WOODEN_EXCAVATOR",
			"STONE_EXCAVATOR",
			"IRON_EXCAVATOR",
			"GOLDEN_EXCAVATOR",
			"DIAMOND_EXCAVATOR",
			"NETHERITE_EXCAVATOR"
	));

	public static ArrayList<String> PLOWS = new ArrayList<String>(Arrays.asList(
			"WOODEN_PLOW",
			"STONE_PLOW",
			"IRON_PLOW",
			"GOLDEN_PLOW",
			"DIAMOND_PLOW",
			"NETHERITE_PLOW"
	));
}

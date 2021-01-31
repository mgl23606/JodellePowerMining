/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.lib;

import com.palmergames.bukkit.towny.TownyMessaging;
import jodelle.powermining.PowerMining;
import jodelle.powermining.crafting.CraftItemExcavator;
import jodelle.powermining.crafting.CraftItemHammer;
import jodelle.powermining.crafting.CraftItemHoe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PowerUtils {
	private static TownyMessaging console;
	public static double CHANCE_FORTUNE_I = 0.33;
	public static double CHANCE_FORTUNE_II = 0.25;
	public static double CHANCE_FORTUNE_III = 0.20;

	public PowerUtils() {}

	// This method checks if the item is a power tool
	public static boolean isPowerTool(ItemStack item) {
		if (item == null || !item.hasItemMeta())
			return false;

		List<String> lore = item.getItemMeta().getLore();

		if (lore == null)
			return false;

		return (Reference.PICKAXES.contains(item.getType()) || Reference.SPADES.contains(item.getType())|| Reference.HOES.contains(item.getType()) &&
				(lore.contains(CraftItemHammer.loreString) || lore.contains(CraftItemExcavator.loreString) || lore.contains(CraftItemHoe.loreString)));
	}

	// This method returns the total amount to be dropped based on fortune level and the normal drop amount
	public static int getAmountPerFortune(int level, int amount) {
		Random rand = new Random();

		if (level == 1 && rand.nextDouble() <= CHANCE_FORTUNE_I)
			return amount * 2;
		else if (level == 2) {
			if (rand.nextDouble() <= CHANCE_FORTUNE_II)
				return amount * 3;
			if (rand.nextDouble() <= CHANCE_FORTUNE_II)
				return amount * 2;
		}
		else if (level == 3) {
			if (rand.nextDouble() <= CHANCE_FORTUNE_III)
				return amount * 4;
			if (rand.nextDouble() <= CHANCE_FORTUNE_III)
				return amount * 3;
			if (rand.nextDouble() <= CHANCE_FORTUNE_III)
				return amount * 2;
		}

		return amount;
	}

	// This method calculates and returns the chance of flint dropping from breaking gravel based on fortune level
	public static double getFlintDropChance(int level) {
		double chance = 0.10;

		if (level == 1)
			chance = 0.14;
		else if (level == 2)
			chance = 0.25;
		else if (level == 3)
			chance = 1.0;

		return chance;
	}

	// This method returns if you can use silk-touch on the block
	public static boolean canSilkTouchMine(Material blockType) {
		return Reference.MINABLE_SILKTOUCH.contains(blockType);
	}
	public static boolean canSilkTouchDig(Material blockType) {
		return Reference.DIGGABLE_SILKTOUCH.contains(blockType);
	}

	// This method returns if you can use fortune on the block
	public static boolean canFortuneMine(Material blockType) {
		return Reference.MINABLE_FORTUNE.get(blockType) != null;
	}
	public static boolean canFortuneDig(Material blockType) {
		return Reference.DIGGABLE_FORTUNE.get(blockType) != null;
	}
	// This method returns if the block is mineable
	public static boolean isMineable(Material blockType) {
		return Reference.MINABLE.containsKey(blockType);
	}

	// This method returns if the block is digable
	public static boolean isDigable(Material blockType) {
		return Reference.DIGGABLE.contains(blockType);
	}
	// This method returns if the block is digable
	public static boolean isFarm(Material blockType) {
		return Reference.FARM.contains(blockType);
	}
	public static boolean isPath(Material blockType) {
		return Reference.PATH.contains(blockType);
	}

	// This method will process the enchantment information and apply to to create the appropriate drop
	public static ItemStack processEnchantsAndReturnItemStack(Enchantment enchant, int enchantLevel, Block block) {
		Material blockType = block.getType();
		ItemStack drop = null;

		if (enchant == Enchantment.SILK_TOUCH)
			drop = new ItemStack(blockType, 1);
		else if (enchant == Enchantment.LOOT_BONUS_BLOCKS) {
			int amount = 0;
			Random rand = new Random();

			if (Reference.MINABLE_FORTUNE.get(blockType) != null) {
				switch (blockType) {
					case GLOWSTONE: // Glowstone drops 2-4 dust, up to 4 max
						amount = Math.min((rand.nextInt(5) + 2) + enchantLevel, 4);

						break;
					case REDSTONE_ORE: // Redstone Ore drops 4-5 dust, up to 8 max (case LEGACY_GLOWING_REDSTONE_ORE:)
						amount = Math.min((rand.nextInt(2) + 4) + enchantLevel, 8);

						break;
					case COAL_ORE: // All these ores drop only 1 item
					case DIAMOND_ORE:
					case ANCIENT_DEBRIS:
					case EMERALD_ORE:
					case NETHER_QUARTZ_ORE:
						amount = getAmountPerFortune(enchantLevel, 1);
						break;
					case LAPIS_ORE: // Lapis Ore drops 4-8 lapis, up to 32 max
						amount = Math.min(getAmountPerFortune(enchantLevel, (rand.nextInt(5) + 4)), 32);
						break;
					default:
						break;
				}

				if (amount > 0) {
					// Lapis needs to be special parsed since it's actually just a DYE with damage value of 4
					if (blockType == Material.LAPIS_ORE)
					//	drop = new ItemStack(Reference.MINABLE_FORTUNE.get(blockType), amount, (short)4);
						drop = new ItemStack(Reference.MINABLE_FORTUNE.get(blockType), amount, (short) 4);
					else
						drop = new ItemStack(Reference.MINABLE_FORTUNE.get(blockType), amount);
				}
			}
			else if (Reference.DIGGABLE_FORTUNE.get(blockType) != null) {
				if (blockType == Material.GLOWSTONE) { // Glowstone drops 2-4 dust, up to 4 max
					amount = Math.min((rand.nextInt(5) + 2) + enchantLevel, 4);

					drop = new ItemStack(Reference.DIGGABLE_FORTUNE.get(blockType), amount);
				}
				else if (blockType == Material.GRAVEL) {
					if (rand.nextDouble() <= getFlintDropChance(enchantLevel))
						drop = new ItemStack(Reference.DIGGABLE_FORTUNE.get(blockType), 1);
					else // If no flint is going to be dropped, drop gravel instead
						drop = new ItemStack(blockType, 1);
				}
			}
		}

		return drop;
	}

	// This method returns a list of surrounding (3x3) blocks given a block face and target block
	public static ArrayList<Block> getSurroundingBlocks(BlockFace blockFace, Block targetBlock, Integer raidus, Integer deep) {
		ArrayList<Block> blocks = new ArrayList<Block>();
		World world = targetBlock.getWorld();

		int bx, by, bz;
		bx = targetBlock.getX();
		by = targetBlock.getY();
		bz = targetBlock.getZ();

		// Check the block face from which the block is being broken in order to get the correct surrounding blocks
		switch(blockFace) {
			case UP:
			case DOWN:
				for(int x = bx - raidus; x <= bx + raidus; x++) {
					for(int y = by - deep; y <= by + deep; y++) {
						for(int z = bz - raidus; z <= bz + raidus; z++) {
							blocks.add(world.getBlockAt(x, by, z));
						}
					}
				}
				break;
			case EAST:
			case WEST:
				for(int x = bx - deep; x <= bx + deep; x++) {
					for(int y = by - raidus; y <= by + raidus; y++) {
						for(int z = bz - raidus; z <= bz + raidus; z++) {
							blocks.add(world.getBlockAt(bx, y, z));
						}
					}
				}
				break;
			case NORTH:
			case SOUTH:
				for(int x = bx - raidus; x <= bx + raidus; x++) {
					for(int y = by - raidus; y <= by + raidus; y++) {
						for(int z = bz - deep; z <= bz + deep; z++) {
							blocks.add(world.getBlockAt(x, y, bz));
						}
					}
				}
				break;
			default:
				break;
		}

		// Trim the nulls from the list
		blocks.removeAll(Collections.singleton(null));
		return blocks;
	}

	// This method returns a list of surrounding (3x3) blocks given a block face and target block
	public static ArrayList<Block> getSurroundingBlocksFarm(BlockFace blockFace, Block targetBlock, Integer raidus) {
		ArrayList<Block> blocks = new ArrayList<Block>();
		World world = targetBlock.getWorld();
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		int bx, by, bz;
		bx = targetBlock.getX();
		by = targetBlock.getY();
		bz = targetBlock.getZ();

		// Check the block face from which the block is being broken in order to get the correct surrounding blocks
		switch(blockFace) {
			case UP:
				for(int x = bx - raidus; x <= bx + raidus; x++) {
					for(int y = by - raidus; y <= by + raidus; y++) {
						for(int z = bz - raidus; z <= bz + raidus; z++) {
							blocks.add(world.getBlockAt(x, by, z));
						}
					}
				}
				break;
			default:
				break;
		}

		// Trim the nulls from the list
		blocks.removeAll(Collections.singleton(null));
		return blocks;
	}

	// This method returns if the player can craft the target item
	public static boolean checkCraftPermission(Player player, Material itemType) {
		boolean canCraft = false;

		switch (itemType) {
			case WOODEN_PICKAXE:
				if (player.hasPermission("powermining.craft.hammer.wood"))
					canCraft = true;

				break;
			case STONE_PICKAXE:
				if (player.hasPermission("powermining.craft.hammer.stone"))
					canCraft = true;

				break;
			case IRON_PICKAXE:
				if (player.hasPermission("powermining.craft.hammer.iron"))
					canCraft = true;

				break;
			case GOLDEN_PICKAXE:
				if (player.hasPermission("powermining.craft.hammer.gold"))
					canCraft = true;

				break;
			case DIAMOND_PICKAXE:
				if (player.hasPermission("powermining.craft.hammer.diamond"))
					canCraft = true;

				break;
			case NETHERITE_PICKAXE:
				if (player.hasPermission("powermining.craft.hammer.netherite"))
					canCraft = true;

				break;
			case WOODEN_SHOVEL:
				if (player.hasPermission("powermining.craft.excavator.wood"))
					canCraft = true;

				break;
			case STONE_SHOVEL:
				if (player.hasPermission("powermining.craft.excavator.stone"))
					canCraft = true;

				break;
			case IRON_SHOVEL:
				if (player.hasPermission("powermining.craft.excavator.iron"))
					canCraft = true;

				break;
			case GOLDEN_SHOVEL:
				if (player.hasPermission("powermining.craft.excavator.gold"))
					canCraft = true;

				break;
			case DIAMOND_SHOVEL:
				if (player.hasPermission("powermining.craft.excavator.diamond"))
					canCraft = true;

				break;
			case NETHERITE_SHOVEL:
				if (player.hasPermission("powermining.craft.excavator.netherite"))
					canCraft = true;

				break;
			case NETHERITE_HOE:
				if (player.hasPermission("powermining.craft.hoe.netherite"))
					canCraft = true;

				break;
			default:
				break;
		}
		return canCraft;
	}

	// This method returns if the player can use the target item
	public static boolean checkUsePermission(Player player, Material itemType) {
		boolean canUse = false;

		switch (player.getInventory().getItemInMainHand().getType()) {
			case WOODEN_PICKAXE:
				if (player.hasPermission("powermining.use.hammer.wood"))
					canUse = true;

				break;
			case STONE_PICKAXE:
				if (player.hasPermission("powermining.use.hammer.stone"))
					canUse = true;

				break;
			case IRON_PICKAXE:
				if (player.hasPermission("powermining.use.hammer.iron"))
					canUse = true;

				break;
			case GOLDEN_PICKAXE:
				if (player.hasPermission("powermining.use.hammer.gold"))
					canUse = true;

				break;
			case DIAMOND_PICKAXE:
				if (player.hasPermission("powermining.use.hammer.diamond"))
					canUse = true;

				break;
			case NETHERITE_PICKAXE:
				if (player.hasPermission("powermining.use.hammer.netherite"))
					canUse = true;

				break;
			case WOODEN_SHOVEL:
				if (player.hasPermission("powermining.use.excavator.wood"))
					canUse = true;

				break;
			case STONE_SHOVEL:
				if (player.hasPermission("powermining.use.excavator.stone"))
					canUse = true;

				break;
			case IRON_SHOVEL:
				if (player.hasPermission("powermining.use.excavator.iron"))
					canUse = true;

				break;
			case GOLDEN_SHOVEL:
				if (player.hasPermission("powermining.use.excavator.gold"))
					canUse = true;

				break;
			case DIAMOND_SHOVEL:
				if (player.hasPermission("powermining.use.excavator.diamond"))
					canUse = true;

				break;
			case NETHERITE_SHOVEL:
				if (player.hasPermission("powermining.use.excavator.netherite"))
					canUse = true;

				break;
			case NETHERITE_HOE:
				if (player.hasPermission("powermining.use.hoe.netherite"))
					canUse = true;

				break;
			default:
				break;
		}

		return canUse;
	}

	// This method returns if the player can enchant the target item
	public static boolean checkEnchantPermission(Player player, Material itemType) {
		boolean canEnchant = false;

		switch (itemType) {
			case WOODEN_PICKAXE:
				if (player.hasPermission("powermining.enchant.hammer.wood"))
					canEnchant = true;

				break;
			case STONE_PICKAXE:
				if (player.hasPermission("powermining.enchant.hammer.stone"))
					canEnchant = true;

				break;
			case IRON_PICKAXE:
				if (player.hasPermission("powermining.enchant.hammer.iron"))
					canEnchant = true;

				break;
			case GOLDEN_PICKAXE:
				if (player.hasPermission("powermining.enchant.hammer.gold"))
					canEnchant = true;

				break;
			case DIAMOND_PICKAXE:
				if (player.hasPermission("powermining.enchant.hammer.diamond"))
					canEnchant = true;

				break;
			case NETHERITE_PICKAXE:
				if (player.hasPermission("powermining.craft.hammer.netherite"))
					canEnchant = true;

				break;
			case WOODEN_SHOVEL:
				if (player.hasPermission("powermining.enchant.excavator.wood"))
					canEnchant = true;

				break;
			case STONE_SHOVEL:
				if (player.hasPermission("powermining.enchant.excavator.stone"))
					canEnchant = true;

				break;
			case IRON_SHOVEL:
				if (player.hasPermission("powermining.enchant.excavator.iron"))
					canEnchant = true;

				break;
			case GOLDEN_SHOVEL:
				if (player.hasPermission("powermining.enchant.excavator.gold"))
					canEnchant = true;

				break;
			case DIAMOND_SHOVEL:
				if (player.hasPermission("powermining.enchant.excavator.diamond"))
					canEnchant = true;

				break;
			case NETHERITE_SHOVEL:
				if (player.hasPermission("powermining.enchant.excavator.netherite"))
					canEnchant = true;

				break;

			case NETHERITE_HOE:
				if (player.hasPermission("powermining.enchant.hoe.netherite"))
					canEnchant = true;

				break;
			default:
				break;
		}

		return canEnchant;
	}

	// This method returns if the player can destroy the target block
	public static boolean canBreak(PowerMining plugin, Player player, Block block) {
		return true;
	}

	// Returns if the tool is a valid hammer against certain block
	public static boolean validateHammer(Material hammerType, Material blockType) {
		return (isMineable(blockType) && Reference.PICKAXES.contains(hammerType) &&
				(Reference.MINABLE.get(blockType) == null || Reference.MINABLE.get(blockType).contains(hammerType)));
	}

	// Returns if the tool is a valid excavator against certain block
	public static boolean validateExcavator(Material excavatorType, Material blockType) {
		return (isDigable(blockType) && Reference.SPADES.contains(excavatorType));
	}

	public static boolean validateHoe(Material hoeType, Material blockType){
		return (isFarm(blockType) && Reference.HOES.contains(hoeType));
	}

	public static boolean validateShovel(Material excavatorType, Material blockType){
		return (isPath(blockType) && Reference.SPADES.contains(excavatorType));
	}
}

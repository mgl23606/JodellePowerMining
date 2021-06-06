/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.lib;

import jodelle.powermining.PowerMining;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;

public class PowerUtils {

    public static double CHANCE_UNBREAKING_I = 0.50;
    public static double CHANCE_UNBREAKING_II = 0.33;
    public static double CHANCE_UNBREAKING_III = 0.25;

    public PowerUtils() {
    }

    // This method checks if the item is a power tool
    // It checks the PersistantDataContainer of the item for a key
    // set specifically for the powertools
    public static boolean isPowerTool(ItemStack item) {

        PowerMining plugin = PowerMining.getInstance();
        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null){
            return false;
        }

		PersistentDataContainer container = itemMeta.getPersistentDataContainer();
		NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");

		return container.has(isPowerTool, PersistentDataType.STRING);
}

    public static void reduceDurability(Player player, ItemStack item){

        if(item.getEnchantments().containsKey(Enchantment.DURABILITY)){
            Random rand = new Random();
            Integer unbreakingLevel = item.getEnchantments().get(Enchantment.DURABILITY);

            if (unbreakingLevel == 1 && rand.nextDouble() > CHANCE_UNBREAKING_I){
                return;
            }
            if (unbreakingLevel == 2 && rand.nextDouble() > CHANCE_UNBREAKING_II){
                return;
            }
            if (unbreakingLevel == 3 && rand.nextDouble() > CHANCE_UNBREAKING_III){
                return;
            }
        }
        ItemMeta itemMeta = item.getItemMeta();

        if (!(itemMeta instanceof Damageable)){
            return;
        }

        Damageable damageable = (Damageable) itemMeta;

        //increasing the damage by one reduces the durability by one
        damageable.setDamage(damageable.getDamage()+1);
        item.setItemMeta(itemMeta);

        /*Reducing the durability doesn't cause the item to be broken when it gets below zero.
        * That said, it is needed to implement this behavior manually.
        * If the damage is higher than the item durability, the item is remover from
        * the player inventory and a breaking sound is played*/
        if (damageable.getDamage() > item.getType().getMaxDurability()){
            player.getInventory().remove(item);

            Location loc = player.getEyeLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
        }


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
        return Reference.TILLABLE.contains(blockType);
    }

    public static boolean isPath(Material blockType) {
        return Reference.PATH.contains(blockType);
    }

    // This method returns a list of surrounding (3x3) blocks given a block face and target block
    public static ArrayList<Block> getSurroundingBlocks(BlockFace blockFace, Block targetBlock, Integer radius, Integer deep) {
        ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();
        int bx, by, bz;
        bx = targetBlock.getX();
        by = targetBlock.getY();
        bz = targetBlock.getZ();

        // Check the block face from which the block is being broken in order to get the correct surrounding blocks
        switch (blockFace) {
            case UP:
                for (int x = bx - radius; x <= bx + radius; x++) {
                    for (int y = by - deep; y <= by; y++) {
                        for (int z = bz - radius; z <= bz + radius; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case DOWN:
                for (int x = bx - radius; x <= bx + radius; x++) {
                    for (int y = by; y <= by + deep; y++) {
                        for (int z = bz - radius; z <= bz + radius; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case EAST:
                for (int x = bx - deep; x <= bx; x++) {
                    for (int y = by - radius; y <= by + radius; y++) {
                        for (int z = bz - radius; z <= bz + radius; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case WEST:
                for (int x = bx; x <= bx + deep; x++) {
                    for (int y = by - radius; y <= by + radius; y++) {
                        for (int z = bz - radius; z <= bz + radius; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case NORTH:
                for (int x = bx - radius; x <= bx + radius; x++) {
                    for (int y = by - radius; y <= by + radius; y++) {
                        for (int z = bz; z <= bz + deep; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case SOUTH:
                //logs.sendMessage(ChatColor.AQUA + "SOUTH");
                for (int x = bx - radius; x <= bx + radius; x++) {
                    for (int y = by - radius; y <= by + radius; y++) {
                        for (int z = bz - deep; z <= bz; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
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
    public static ArrayList<Block> getSurroundingBlocksFarm(BlockFace blockFace, Block targetBlock, Integer radius) {
        ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();
        int bx, by, bz;
        bx = targetBlock.getX();
        by = targetBlock.getY();
        bz = targetBlock.getZ();

        // Check the block face from which the block is being broken in order to get the correct surrounding blocks
        if (blockFace == BlockFace.UP) {
            for (int x = bx - radius; x <= bx + radius; x++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    blocks.add(world.getBlockAt(x, by, z));
                }
            }
        }

        // Trim the nulls from the list
        blocks.removeAll(Collections.singleton(null));
        return blocks;
    }

    // This method returns if the player can craft the target item
    public static boolean checkCraftPermission(Player player, Material itemType) {
        if (!Reference.CRAFT_PERMISSIONS.containsKey(itemType)){
            throw new NoSuchElementException();
        }

        String perm = Reference.CRAFT_PERMISSIONS.get(itemType);

        return player.hasPermission(perm);
    }

    // This method returns if the player can use the target item
    public static boolean checkUsePermission(Player player, Material itemType) {
        if (!Reference.USE_PERMISSIONS.containsKey(itemType)){
            throw new NoSuchElementException();
        }

        String perm = Reference.USE_PERMISSIONS.get(itemType);

        return player.hasPermission(perm);
    }

    // This method returns if the player can enchant the target item
    public static boolean checkEnchantPermission(Player player, Material itemType) {
        if (!Reference.ENCHANT_PERMISSIONS.containsKey(itemType)){
            throw new NoSuchElementException();
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String perm = Reference.ENCHANT_PERMISSIONS.get(itemType);
        console.sendMessage(ChatColor.GOLD+ perm);

        return player.hasPermission(perm);

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

    public static boolean validatePlow(Material hoeType, Material blockType) {
        return (isFarm(blockType) && Reference.HOES.contains(hoeType));
    }

    public static boolean validatePath(Material excavatorType, Material blockType) {
        return (isPath(blockType) && Reference.SPADES.contains(excavatorType));
    }
}

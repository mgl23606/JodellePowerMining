/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.lib;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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

import javax.annotation.Nonnull;
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

    /**
     * This method validates if the item is a PowerTool
     * It checks the PersistantDataContainer of the item for a key
     * set specifically for the PowerTools
     * @param item The item to test
     * @return True if the item is a PowerTool
     */
    public static boolean isPowerTool(@Nonnull ItemStack item) {

        PowerMining plugin = PowerMining.getInstance();
        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null){
            return false;
        }

		PersistentDataContainer container = itemMeta.getPersistentDataContainer();
		NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");

		return container.has(isPowerTool, PersistentDataType.STRING);
}

    /**
     * Reduces the durability of a tool
     * @param player Player who used the tool
     * @param item Item used by the player
     */
    public static void reduceDurability(@Nonnull Player player, @Nonnull ItemStack item){

        if(item.getEnchantments().containsKey(Enchantment.DURABILITY)){
            Random rand = new Random();
            Integer unbreakingLevel = item.getEnchantments().get(Enchantment.DURABILITY);
            if (rand.nextInt(100/unbreakingLevel+1) == 0){
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
            World world = loc.getWorld();
            if (world != null) {
                world.playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            }
        }


    }


    /**
     * Returns if the block is present on the minable list
     * @param blockType Block to be verified
     * @return True if the block is minable
     */
    public static boolean isMineable(@Nonnull Material blockType) {
        return Reference.MINABLE.containsKey(blockType);
    }

    /**
     * Returns if the block is present on the digable list
     * @param blockType Block to be verified
     * @return True if the block is digable
     */
    public static boolean isDigable(@Nonnull Material blockType) {
        return Reference.DIGGABLE.contains(blockType);
    }

    /**
     * Returns if the block is present on the tillable list
     * @param blockType Block to be verified
     * @return True if the block is tillable
     */
    public static boolean isTillable(@Nonnull Material blockType) {
        return Reference.TILLABLE.contains(blockType);
    }

    /**
     * Returns if the block is present on the pathable list
     * @param blockType Block to be verified
     * @return True if the block is pathable
     */
    public static boolean isPathable(@Nonnull Material blockType) {
        return Reference.PATHABLE.contains(blockType);
    }

    /**
     * Returns a lif of the surrounding blocks given a block face, a target block, a radius and a depth
     * @param blockFace Face of the block where the player clicked
     * @param targetBlock Block broke by the player
     * @param radius Radius of the hole
     * @param depth Depth of the hole
     * @return Array containing all the blocks surrounding the target block
     */
    @Nonnull
    public static ArrayList<Block> getSurroundingBlocks(@Nonnull BlockFace blockFace, @Nonnull Block targetBlock, @Nonnull Integer radius, @Nonnull Integer depth) {
        //Array that will store the blocks surrounding the center block
        final ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();

        int bx, by, bz;
        bx = targetBlock.getX();
        by = targetBlock.getY();
        bz = targetBlock.getZ();

        // Check the block face from which the block is being broken in order to get the correct surrounding blocks
        switch (blockFace) {
            case UP:
                for (int x = bx - radius; x <= bx + radius; x++) {
                    for (int y = by - depth; y <= by; y++) {
                        for (int z = bz - radius; z <= bz + radius; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case DOWN:
                for (int x = bx - radius; x <= bx + radius; x++) {
                    for (int y = by; y <= by + depth; y++) {
                        for (int z = bz - radius; z <= bz + radius; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case EAST:
                for (int x = bx - depth; x <= bx; x++) {
                    for (int y = by - radius; y <= by + radius; y++) {
                        for (int z = bz - radius; z <= bz + radius; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case WEST:
                for (int x = bx; x <= bx + depth; x++) {
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
                        for (int z = bz; z <= bz + depth; z++) {
                            blocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                break;
            case SOUTH:
                //logs.sendMessage(ChatColor.AQUA + "SOUTH");
                for (int x = bx - radius; x <= bx + radius; x++) {
                    for (int y = by - radius; y <= by + radius; y++) {
                        for (int z = bz - depth; z <= bz; z++) {
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

    /**
     * Returns a lif of the surrounding blocks given a block face, a target block, a radius and a depth
     * @param blockFace Face of the block where the player clicked
     * @param targetBlock Block broke by the player
     * @param radius Radius of the hole
     * @return Array containing all the blocks surrounding the target block
     */
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

    /**
     * Checks if the player has permission to craft the PowerTool
     * @param player Player who used the tool
     * @param itemType Item used by the player
     * @return True if the player has permission to craft the PowerTool
     */
    public static boolean checkCraftPermission(@Nonnull Player player, @Nonnull Material itemType) {
        if (!Reference.CRAFT_PERMISSIONS.containsKey(itemType)){
            throw new NoSuchElementException();
        }

        String perm = Reference.CRAFT_PERMISSIONS.get(itemType);

        return player.hasPermission(perm);
    }

    /**
     * Checks if the player has permission to use the PowerTool
     * @param player Player who used the tool
     * @param itemType Item used by the player
     * @return True if the player has permission to use the PowerTool
     */
    public static boolean checkUsePermission(@Nonnull Player player, @Nonnull Material itemType) {
        if (!Reference.USE_PERMISSIONS.containsKey(itemType)){
            throw new NoSuchElementException();
        }

        String perm = Reference.USE_PERMISSIONS.get(itemType);

        return player.hasPermission(perm);
    }

    /**
     * Checks if the player has permission to enchant the PowerTool
     * @param player Player who used the tool
     * @param itemType Item used by the player
     * @return True if the player has permission to enchant the PowerTool
     */
    public static boolean checkEnchantPermission(@Nonnull Player player, @Nonnull Material itemType) {
        if (!Reference.ENCHANT_PERMISSIONS.containsKey(itemType)){
            throw new NoSuchElementException();
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String perm = Reference.ENCHANT_PERMISSIONS.get(itemType);

        return player.hasPermission(perm);
    }

    /**
     * Checks if the player is allowed to destroy the target block
     * @param plugin Instance of the plugin
     * @param player Player who broke the block
     * @param block Block broken
     * @return True if the player is allowed to destroy the block
     */
    public static boolean canBreak(@Nonnull PowerMining plugin, @Nonnull Player player, @Nonnull Block block) {

        if (plugin.getWorldGuard() == null){
            return true;
        }

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(block.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (!query.testBuild(loc, localPlayer, Flags.BUILD)) {
            plugin.getDebuggingMessages().sendConsoleMessage("Can't build");
            return false;
        }

        plugin.getDebuggingMessages().sendConsoleMessage("Can build");
        return true;
    }

    /**
     * Returns if the tool is a valid Hammer against certain block
     * @param hammerType Type of the tool
     * @param blockType Type of the block to be used on
     * @return True if the Hammer is valid
     */
    public static boolean validateHammer(@Nonnull Material hammerType, @Nonnull Material blockType) {
        return (isMineable(blockType) && Reference.PICKAXES.contains(hammerType) &&
                (Reference.MINABLE.get(blockType) == null || Reference.MINABLE.get(blockType).contains(hammerType)));
    }

    /**
     * Returns if the tool is a valid Excavator against certain block
     * @param excavatorType Type of the tool
     * @param blockType Type of the block to be used on
     * @return True if the Excavator is valid
     */
    public static boolean validateExcavator(@Nonnull Material excavatorType, @Nonnull Material blockType) {
        return (isDigable(blockType) && Reference.SPADES.contains(excavatorType));
    }

    /**
     * Returns if the tool is a valid Plow against certain block
     * @param plowType Type of the tool
     * @param blockType Type of the block to be used on
     * @return True if the Plow is valid
     */
    public static boolean validatePlow(@Nonnull Material plowType, @Nonnull Material blockType) {
        return (isTillable(blockType) && Reference.HOES.contains(plowType));
    }

    /**
     * Returns if the tool is a valid Excavator against certain block
     * @param excavatorType Type of the tool
     * @param blockType Type of the block to be used on
     * @return True if the Excavator is valid
     */
    public static boolean validatePath(@Nonnull Material excavatorType, @Nonnull Material blockType) {
        return (isPathable(blockType) && Reference.SPADES.contains(excavatorType));
    }
}

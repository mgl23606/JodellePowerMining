package jodelle.powermining.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Utility class for handling PowerTools-related logic in the PowerMining
 * plugin.
 * 
 * <p>
 * This class provides a collection of static methods to check tool types,
 * validate permissions,
 * handle durability reduction, and interact with blocks in the game world. It
 * also integrates
 * with WorldGuard to enforce region protection rules.
 * </p>
 */
public class PowerUtils {

    /**
     * Constructs a {@code PowerUtils} instance.
     * 
     * <p>
     * This constructor is private since all methods in this class are static,
     * meaning an instance of {@code PowerUtils} is never needed.
     * </p>
     */
    public PowerUtils() {
    }

    /**
     * Checks if an {@link ItemStack} is a PowerTool by verifying its
     * {@link PersistentDataContainer}.
     * 
     * <p>
     * This method looks for a specific {@link NamespacedKey} in the item's metadata
     * to determine if
     * it is a PowerTool.
     * </p>
     * 
     * @param item The {@link ItemStack} to check.
     * @return {@code true} if the item is a PowerTool, {@code false} otherwise.
     */
    public static boolean isPowerTool(@Nonnull ItemStack item) {

        PowerMining plugin = PowerMining.getInstance();
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        NamespacedKey isPowerTool = new NamespacedKey(plugin, "isPowerTool");

        return container.has(isPowerTool, PersistentDataType.STRING);
    }

    /**
     * Reduces the durability of a tool when it is used.
     * 
     * <p>
     * If the tool has the {@link Enchantment#UNBREAKING} enchantment, there is a
     * chance
     * that durability loss will be prevented based on the enchantment level. If the
     * tool
     * reaches zero durability, it is removed from the player's inventory and a
     * breaking
     * sound effect is played.
     * </p>
     * 
     * @param player The {@link Player} using the tool.
     * @param item   The {@link ItemStack} representing the tool.
     */
    public static void reduceDurability(@Nonnull Player player, @Nonnull ItemStack item) {

        if (item.getEnchantments().containsKey(Enchantment.UNBREAKING)) {
            Random rand = new Random();
            Integer unbreakingLevel = item.getEnchantments().get(Enchantment.UNBREAKING);
            if (rand.nextInt(unbreakingLevel + 1) != 0) {
                return;
            }
        }
        ItemMeta itemMeta = item.getItemMeta();

        if (!(itemMeta instanceof Damageable)) {
            return;
        }

        Damageable damageable = (Damageable) itemMeta;

        // increasing the damage by one reduces the durability by one
        damageable.setDamage(damageable.getDamage() + 1);
        item.setItemMeta(itemMeta);

        /*
         * Reducing the durability doesn't cause the item to be broken when it gets
         * below zero.
         * That said, it is needed to implement this behavior manually.
         * If the damage is higher than the item durability, the item is remover from
         * the player inventory and a breaking sound is played
         */
        if (damageable.getDamage() > item.getType().getMaxDurability() - 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));  
            player.updateInventory(); 
    
            // Play item break sound
            Location loc = player.getEyeLocation();
            World world = loc.getWorld();
            if (world != null) {
                world.playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            }
        }
    }

    /**
     * Determines whether a given block type is minable.
     * 
     * @param blockType The {@link Material} of the block.
     * @return {@code true} if the block is minable, {@code false} otherwise.
     */
    public static boolean isMineable(@Nonnull Material blockType) {
        return Reference.MINABLE.containsKey(blockType);
    }

    /**
     * Determines whether a given block type is diggable.
     * 
     * @param blockType The {@link Material} of the block.
     * @return {@code true} if the block is diggable, {@code false} otherwise.
     */
    public static boolean isDigable(@Nonnull Material blockType) {
        return Reference.DIGGABLE.contains(blockType);
    }

    /**
     * Determines whether a given block type is tillable.
     * 
     * @param blockType The {@link Material} of the block.
     * @return {@code true} if the block is tillable, {@code false} otherwise.
     */
    public static boolean isTillable(@Nonnull Material blockType) {
        return Reference.TILLABLE.contains(blockType);
    }

    /**
     * Determines whether a given block type is pathable.
     * 
     * @param blockType The {@link Material} of the block.
     * @return {@code true} if the block is pathable, {@code false} otherwise.
     */
    public static boolean isPathable(@Nonnull Material blockType) {
        return Reference.PATHABLE.contains(blockType);
    }

    /**
     * Retrieves a list of surrounding blocks based on the block face clicked, the
     * target block,
     * and the specified radius and depth.
     * 
     * <p>
     * This method determines which blocks should be affected by the player's
     * action, depending
     * on the block face interacted with. It generates a list of blocks in a cubic
     * or rectangular
     * shape, trimming any null values.
     * </p>
     * 
     * @param blockFace   The {@link BlockFace} indicating the direction the player
     *                    interacted with.
     * @param targetBlock The {@link Block} the player broke.
     * @param radius      The radius of the area to affect.
     * @param depth       The depth of the area to affect.
     * @return A list of surrounding {@link Block}s affected by the player's action.
     */
    @Nonnull
    public static ArrayList<Block> getSurroundingBlocks(@Nonnull BlockFace blockFace, @Nonnull Block targetBlock,
            @Nonnull Integer radius, @Nonnull Integer depth) {
        // Array that will store the blocks surrounding the center block
        final ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();

        int bx, by, bz;
        bx = targetBlock.getX();
        by = targetBlock.getY();
        bz = targetBlock.getZ();

        // Check the block face from which the block is being broken in order to get the
        // correct surrounding blocks
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
                // logs.sendMessage(ChatColor.AQUA + "SOUTH");
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
     * Retrieves a list of surrounding blocks for farming purposes based on the
     * block face clicked.
     * 
     * <p>
     * This method is a specialized version of {@link #getSurroundingBlocks} used
     * primarily for
     * plowing or path creation. It only checks horizontally, as farmland and paths
     * are typically
     * affected on a flat plane.
     * </p>
     * 
     * @param blockFace   The {@link BlockFace} indicating the direction the player
     *                    interacted with.
     * @param targetBlock The {@link Block} being affected.
     * @param radius      The radius of the area to affect.
     * @return A list of surrounding {@link Block}s affected by the farming tool.
     */
    public static ArrayList<Block> getSurroundingBlocksFarm(BlockFace blockFace, Block targetBlock, Integer radius) {
        ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();
        int bx, by, bz;
        bx = targetBlock.getX();
        by = targetBlock.getY();
        bz = targetBlock.getZ();

        // Check the block face from which the block is being broken in order to get the
        // correct surrounding blocks
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
     * Checks if the player has permission to craft a PowerTool.
     * 
     * @param plugin   The {@link PowerMining} instance managing permissions.
     * @param player   The {@link Player} attempting to craft the tool.
     * @param itemType The {@link Material} type of the tool being crafted.
     * @return {@code true} if the player has permission, {@code false} otherwise.
     * @throws NoSuchElementException if the tool is not listed in the craft
     *                                permissions.
     */
    public static boolean checkCraftPermission(@Nonnull PowerMining plugin, @Nonnull Player player,
            @Nonnull Material itemType) {
        if (!Reference.CRAFT_PERMISSIONS.containsKey(itemType)) {
            throw new NoSuchElementException();
        }

        String perm = Reference.CRAFT_PERMISSIONS.get(itemType);

        if (player.hasPermission(perm)) {
            return true;
        }

        player.sendMessage(plugin.getLangFile().getMessage("no-craft-permission",
                "&cYou don't have permissions to craft this PowerTool!", true));
        return false;
    }

    /**
     * Checks if the player has permission to use a PowerTool.
     * 
     * @param plugin   The {@link PowerMining} instance managing permissions.
     * @param player   The {@link Player} attempting to use the tool.
     * @param itemType The {@link Material} type of the tool being used.
     * @return {@code true} if the player has permission, {@code false} otherwise.
     */
    public static boolean checkUsePermission(@NonNull PowerMining plugin, @Nonnull Player player,
            @Nonnull Material itemType) {
        if (!Reference.USE_PERMISSIONS.containsKey(itemType)) {
            throw new NoSuchElementException();
        }

        String perm = Reference.USE_PERMISSIONS.get(itemType);

        if (player.hasPermission(perm)) {
            return true;
        }

        player.sendMessage(plugin.getLangFile().getMessage("no-use-permission",
                "&cYou don't have permissions to use this PowerTool!", true));
        return false;
    }

    /**
     * Checks if the player has permission to enchant a PowerTool.
     * 
     * @param plugin   The {@link PowerMining} instance managing permissions.
     * @param player   The {@link Player} attempting to enchant the tool.
     * @param itemType The {@link Material} type of the tool being enchanted.
     * @return {@code true} if the player has permission, {@code false} otherwise.
     */
    public static boolean checkEnchantPermission(@Nonnull PowerMining plugin, @Nonnull Player player,
            @Nonnull Material itemType) {
        if (!Reference.ENCHANT_PERMISSIONS.containsKey(itemType)) {
            throw new NoSuchElementException();
        }
        String perm = Reference.ENCHANT_PERMISSIONS.get(itemType);

        if (player.hasPermission(perm)) {
            return true;
        }

        player.sendMessage(plugin.getLangFile().getMessage("no-enchant-permission",
                "&cYou don't have permissions to enchant this PowerTool!", true));
        return false;
    }

    /**
     * Checks if the player is allowed to destroy the target block based on
     * WorldGuard region protections.
     * 
     * <p>
     * This method verifies whether the player has permission to break a block in a
     * protected region using WorldGuard. If WorldGuard is not enabled, it assumes
     * the player is allowed to break the block.
     * </p>
     * 
     * @param plugin The {@link PowerMining} instance managing WorldGuard
     *               integration.
     * @param player The {@link Player} attempting to break the block.
     * @param block  The {@link Block} the player is trying to destroy.
     * @return {@code true} if the player is allowed to break the block,
     *         {@code false} otherwise.
     */
    public static boolean canBreak(@Nonnull PowerMining plugin, @Nonnull Player player, @Nonnull Block block) {

        if (plugin.getWorldGuard() == null) {
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
     * Determines if the given tool is a valid Hammer for breaking a specific block
     * type.
     * 
     * <p>
     * This method checks whether the specified hammer type is allowed to mine the
     * given
     * block type, ensuring compatibility with the configured list of minable
     * materials.
     * </p>
     * 
     * @param hammerType The {@link Material} representing the hammer.
     * @param blockType  The {@link Material} representing the block to be mined.
     * @return {@code true} if the hammer is valid for breaking the block,
     *         {@code false} otherwise.
     */
    public static boolean validateHammer(@Nonnull Material hammerType, @Nonnull Material blockType) {
        return (isMineable(blockType) && Reference.PICKAXES.contains(hammerType) &&
                (Reference.MINABLE.get(blockType) == null || Reference.MINABLE.get(blockType).contains(hammerType)));
    }

    /**
     * Determines if the given tool is a valid Excavator for digging a specific
     * block type.
     * 
     * <p>
     * This method checks if the specified excavator type is appropriate for digging
     * the given block type, ensuring it aligns with the predefined list of diggable
     * materials.
     * </p>
     * 
     * @param excavatorType The {@link Material} representing the excavator.
     * @param blockType     The {@link Material} representing the block to be
     *                      excavated.
     * @return {@code true} if the excavator is valid for digging the block,
     *         {@code false} otherwise.
     */
    public static boolean validateExcavator(@Nonnull Material excavatorType, @Nonnull Material blockType) {
        return (isDigable(blockType) && Reference.SPADES.contains(excavatorType));
    }

    /**
     * Determines if the given tool is a valid Plow for tilling a specific block
     * type.
     * 
     * <p>
     * This method checks if the specified plow type can till the given block type,
     * ensuring it matches the configured list of tillable materials.
     * </p>
     * 
     * @param plowType  The {@link Material} representing the plow.
     * @param blockType The {@link Material} representing the block to be tilled.
     * @return {@code true} if the plow is valid for tilling the block,
     *         {@code false} otherwise.
     */
    public static boolean validatePlow(@Nonnull Material plowType, @Nonnull Material blockType) {
        return (isTillable(blockType) && Reference.HOES.contains(plowType));
    }

    /**
     * Determines if the given tool is a valid Excavator for creating paths on a
     * specific block type.
     * 
     * <p>
     * This method checks if the specified excavator type is suitable for converting
     * the
     * given block type into a dirt path, ensuring it is part of the predefined list
     * of pathable materials.
     * </p>
     * 
     * @param excavatorType The {@link Material} representing the excavator.
     * @param blockType     The {@link Material} representing the block to be
     *                      converted into a path.
     * @return {@code true} if the excavator is valid for creating a path,
     *         {@code false} otherwise.
     */
    public static boolean validatePath(@Nonnull Material excavatorType, @Nonnull Material blockType) {
        return (isPathable(blockType) && Reference.SPADES.contains(excavatorType));
    }
}

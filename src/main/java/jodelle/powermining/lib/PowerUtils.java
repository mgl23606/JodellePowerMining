/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;

public final class PowerUtils {

    private PowerUtils() {
        // Prevent instantiation — this is a utility class.
    }

    /**
     * Checks whether an item is one of our custom power tools
     * by looking for our tracking key in its PersistentDataContainer.
     */
    public static boolean isPowerTool(@NotNull ItemStack item) {
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
     * Handles tool durability loss, checks for unbreaking enchantment rolls,
     * and deletes the item with a sound effect if it breaks completely.
     */
    public static void reduceDurability(@NotNull Player player, @NotNull ItemStack item) {
        // Grab the unbreaking enchantment using the modern registry API
        final Enchantment UNBREAKING = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));

        // Factor in unbreaking enchantments if present on the tool
        if (item.hasItemMeta() && UNBREAKING != null && item.getItemMeta().hasEnchant(UNBREAKING)) {
            Random rand = new Random();
            int unbreakingLevel = item.getItemMeta().getEnchantLevel(UNBREAKING);
            int chanceToAvoidLoss = unbreakingLevel + 1;

            if (rand.nextInt(chanceToAvoidLoss) != 0) {
                return; // Durability loss successfully prevented by unbreaking
            }
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof Damageable damageable)) {
            return;
        }

        // Apply 1 point of damage wear
        damageable.setDamage(damageable.getDamage() + 1);
        item.setItemMeta(itemMeta);

        // If damage exceeds maximum item durability, remove the tool from inventory and play break sound
        if (damageable.getDamage() > item.getType().getMaxDurability()) {
            player.getInventory().remove(item);

            Location loc = player.getEyeLocation();
            World world = loc.getWorld();
            if (world != null) {
                world.playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            }
        }
    }

    public static boolean isMineable(@NotNull Material blockType) {
        return Reference.MINABLE.containsKey(blockType);
    }

    public static boolean isDigable(@NotNull Material blockType) {
        return Reference.DIGGABLE.contains(blockType);
    }

    public static boolean isTillable(@NotNull Material blockType) {
        return Reference.TILLABLE.contains(blockType);
    }

    public static boolean isPathable(@NotNull Material blockType) {
        return Reference.PATHABLE.contains(blockType);
    }

    /**
     * Calculates all surrounding blocks to break based on the clicked block face, radius, and depth.
     */
    @NotNull
    public static ArrayList<Block> getSurroundingBlocks(@NotNull BlockFace blockFace, @NotNull Block targetBlock, @NotNull Integer radius, @NotNull Integer depth) {
        final ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();

        int bx = targetBlock.getX();
        int by = targetBlock.getY();
        int bz = targetBlock.getZ();

        // Determine sweep direction based on which face of the block the player interacted with
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

        blocks.removeAll(Collections.singleton(null));
        return blocks;
    }

    /**
     * Calculates surrounding blocks specifically for farming actions (tilling and paths) in a flat plane.
     */
    @NotNull
    public static ArrayList<Block> getSurroundingBlocksFarm(@NotNull BlockFace blockFace, @NotNull Block targetBlock, @NotNull Integer radius) {
        ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();
        int bx = targetBlock.getX();
        int by = targetBlock.getY();
        int bz = targetBlock.getZ();

        if (blockFace == BlockFace.UP) {
            for (int x = bx - radius; x <= bx + radius; x++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    blocks.add(world.getBlockAt(x, by, z));
                }
            }
        }

        blocks.removeAll(Collections.singleton(null));
        return blocks;
    }

    public static boolean checkCraftPermission(@NotNull Player player, @NotNull Material itemType) {
        if (!Reference.CRAFT_PERMISSIONS.containsKey(itemType)) {
            throw new NoSuchElementException();
        }

        String perm = Reference.CRAFT_PERMISSIONS.get(itemType);
        if (player.hasPermission(perm)) {
            return true;
        }

        player.sendMessage(ChatColor.RED + "[JodellePowerMining] - You don't have permissions to craft this PowerTool");
        return false;
    }

    public static boolean checkUsePermission(@NotNull Player player, @NotNull Material itemType) {
        if (!Reference.USE_PERMISSIONS.containsKey(itemType)) {
            throw new NoSuchElementException();
        }

        String perm = Reference.USE_PERMISSIONS.get(itemType);
        if (player.hasPermission(perm)) {
            return true;
        }

        player.sendMessage(ChatColor.RED + "[JodellePowerMining] - You don't have permissions to use this PowerTool");
        return false;
    }

    public static boolean checkEnchantPermission(@NotNull Player player, @NotNull Material itemType) {
        if (!Reference.ENCHANT_PERMISSIONS.containsKey(itemType)) {
            throw new NoSuchElementException();
        }

        String perm = Reference.ENCHANT_PERMISSIONS.get(itemType);
        if (player.hasPermission(perm)) {
            return true;
        }

        player.sendMessage(ChatColor.RED + "[JodellePowerMining] - You don't have permissions to enchant this PowerTool");
        return false;
    }

    /**
     * Integrates with WorldGuard to verify if a player has building and breaking rights at a specific location.
     */
    public static boolean canBreak(@NotNull PowerMining plugin, @NotNull Player player, @NotNull Block block) {
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

    public static boolean validateHammer(@NotNull Material hammerType, @NotNull Material blockType) {
        return (isMineable(blockType) && Reference.PICKAXES.contains(hammerType) &&
                (Reference.MINABLE.get(blockType) == null || Reference.MINABLE.get(blockType).contains(hammerType)));
    }

    public static boolean validateExcavator(@NotNull Material excavatorType, @NotNull Material blockType) {
        return (isDigable(blockType) && Reference.SPADES.contains(excavatorType));
    }

    public static boolean validatePlow(@NotNull Material plowType, @NotNull Material blockType) {
        return (isTillable(blockType) && Reference.HOES.contains(plowType));
    }

    public static boolean validatePath(@NotNull Material excavatorType, @NotNull Material blockType) {
        return (isPathable(blockType) && Reference.SPADES.contains(excavatorType));
    }
}
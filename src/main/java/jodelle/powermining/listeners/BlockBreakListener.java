package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Listener for handling {@link BlockBreakEvent} in the PowerMining plugin.
 * 
 * <p>
 * This class manages the mechanics of breaking blocks using custom PowerTools
 * such as Hammers and Excavators. It determines the area of effect, reduces
 * tool durability, grants experience, and integrates with job-tracking plugins.
 * </p>
 */
public class BlockBreakListener implements Listener {
    public final PowerMining plugin;
    public final boolean useDurabilityPerBlock;
    public final DebuggingMessages debuggingMessages;

    public BlockBreakListener(@Nonnull PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        debuggingMessages = plugin.getDebuggingMessages();
        useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    /**
     * Handles block breaking events and applies PowerTool effects.
     * 
     * <p>
     * This method verifies that the player is using a PowerTool, determines the
     * affected blocks, reduces durability, grants XP, and integrates with
     * JobsReborn if enabled.
     * </p>
     * 
     * @param event The {@link BlockBreakEvent} triggered when a block is broken.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            return;
        }

        if (handItem.getItemMeta() != null && handItem.getItemMeta().hasDisplayName()) {
            debuggingMessages.sendConsoleMessage(
                    ChatColor.RED + "Broke a block with item: " + handItem.getItemMeta().getDisplayName());
        }

        if (basicVerifications(player, handItem)) { // ✅ Pass player & handItem explicitly
            return;
        }

        final Block centerBlock = event.getBlock();
        final String playerName = player.getName();
        final PlayerInteractListener pil = plugin.getPlayerInteractHandler() != null
                ? plugin.getPlayerInteractHandler().getListener()
                : null;

        if (pil == null) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "PlayerInteractListener is null.");
            return;
        }

        final BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

        int radius = Math.max(0, plugin.getConfig().getInt("Radius", Reference.RADIUS) - 1);
        int depth = Math.max(0, plugin.getConfig().getInt("Depth", Reference.DEPTH) - 1);

        List<Block> surroundingBlocks = PowerUtils.getSurroundingBlocks(blockFace, centerBlock, radius, depth);

        if (surroundingBlocks.isEmpty()) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "No surrounding blocks found.");
            return;
        }

        // Handle durability for the center block
        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            PowerUtils.reduceDurability(player, handItem);
            player.getInventory().setItemInMainHand(handItem);
        }

        for (Block block : surroundingBlocks) {
            int exp = checkAndBreakBlock(player, handItem, block);

            if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                PowerUtils.reduceDurability(player, handItem);
                player.getInventory().setItemInMainHand(handItem);
            }

            if (exp > 0) {
                BlockExpEvent expEvent = new BlockExpEvent(block, exp);
                plugin.getServer().getPluginManager().callEvent(expEvent);

                ExperienceOrb orb = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
                orb.setExperience(expEvent.getExpToDrop());
            }
        }
    }

    /**
     * Checks and breaks a block if it is compatible with the PowerTool.
     * 
     * <p>
     * This method validates whether the tool can break the given block, notifies
     * JobsReborn (if enabled), and determines the experience points (XP) to drop.
     * </p>
     * 
     * @param player   The player breaking the block.
     * @param handItem The tool being used to break the block.
     * @param block    The {@link Block} being broken.
     * @return The amount of XP to drop from breaking the block.
     */
    private int checkAndBreakBlock(Player player, ItemStack handItem, @Nonnull Block block) {
        if (handItem == null || handItem.getType() == Material.AIR) {
            return 0; // No tool, no block breaking
        }

        Material blockMat = block.getType();
        boolean useHammer = PowerUtils.validateHammer(handItem.getType(), blockMat);
        boolean useExcavator = !useHammer && PowerUtils.validateExcavator(handItem.getType(), blockMat);

        if (useHammer || useExcavator) {
            if (!PowerUtils.canBreak(plugin, player, block)) {
                return 0;
            }

            // Notify Jobs Reborn BEFORE breaking the block
            if (plugin.getJobsHook() != null) {
                plugin.getJobsHook().notifyJobs(player, block);
                debuggingMessages
                        .sendConsoleMessage(ChatColor.GREEN + "✅ Jobs Reborn notified for block: " + block.getType());
            }

            // XP Drops
            int expToDrop = switch (blockMat) {
                case COAL_ORE, DEEPSLATE_COAL_ORE -> (int) (Math.random() * 3); // 0-2 XP
                case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, EMERALD_ORE, DEEPSLATE_EMERALD_ORE ->
                    3 + (int) (Math.random() * 5); // 3-7 XP
                case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE, LAPIS_ORE, DEEPSLATE_LAPIS_ORE ->
                    2 + (int) (Math.random() * 4); // 2-6 XP
                case NETHER_QUARTZ_ORE -> 2 + (int) (Math.random() * 3); // 2-5 XP
                case NETHER_GOLD_ORE -> 1 + (int) (Math.random() * 5); // 1-5 XP
                case SPAWNER -> 15 + (int) (Math.random() * 30); // 15-43 XP
                default -> 0; // No XP for blocks not listed
            };

            // Break the block naturally if conditions are met
            if (block.breakNaturally(handItem) && player.getGameMode().equals(GameMode.SURVIVAL)) {
                if (plugin.getConfig().getBoolean("useDurabilityPerBlock")) {
                    PowerUtils.reduceDurability(player, handItem);
                }
            }

            return expToDrop; // Return XP only for eligible blocks
        }

        return 0; // Return 0 XP if conditions aren't met
    }

    /**
     * Performs basic verifications before applying PowerTool effects.
     * 
     * <p>
     * This method ensures the player is using a valid PowerTool, has permissions,
     * and is not sneaking to bypass the special tool mechanics.
     * </p>
     * 
     * @param player   The player using the tool.
     * @param handItem The tool being used.
     * @return {@code true} if the tool should behave normally, {@code false} if
     *         PowerTool mechanics apply.
     */
    private boolean basicVerifications(Player player, ItemStack handItem) {
        if (handItem == null || handItem.getType() == Material.AIR) {
            return true;
        }

        if (player.isSneaking()) {
            return true;
        }

        Material handItemType = handItem.getType();

        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }

        if (!PowerUtils.checkUsePermission(plugin, player, handItemType)) {
            return true;
        }

        return false;
    }
}

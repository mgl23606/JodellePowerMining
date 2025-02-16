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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Listener for handling {@link PlayerInteractEvent} when using PowerTools on
 * blocks.
 * 
 * <p>
 * This class manages interactions where players use custom PowerTools to till
 * soil
 * or create paths by right-clicking blocks. It validates interactions, modifies
 * blocks
 * accordingly, and reduces tool durability.
 * </p>
 */
public class ClickPlayerListener implements Listener {
    private final PowerMining plugin;
    private final boolean useDurabilityPerBlock;
    private final DebuggingMessages debuggingMessages;

    /**
     * Constructs a {@code ClickPlayerListener} and registers it as an event
     * listener.
     * 
     * @param plugin The instance of {@link PowerMining} used for event
     *               registration.
     */
    public ClickPlayerListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        debuggingMessages = plugin.getDebuggingMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    /**
     * Handles player interactions with blocks using PowerTools.
     * 
     * <p>
     * This method verifies whether a PowerTool is being used and modifies
     * surrounding blocks
     * to farmland or dirt paths accordingly. It also reduces the tool's durability
     * when applicable.
     * </p>
     * 
     * @param event The {@link PlayerInteractEvent} triggered when a player
     *              interacts with a block.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUse(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack handItem = player.getInventory().getItemInMainHand();
        final Material handItemType = handItem.getType();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        if (basicVerifications(action, player, handItem, handItemType, block)) {
            return;
        }

        final String playerName = player.getName();

        final PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
        final BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

        /*
         * At this point intellij shows a warning about the possibility of the argument
         * block being null. This warning can be ignored because in the method
         * basicVerifications()
         * we already make sure that the block is not null, and if it is indeed null
         * this method
         * never reached this point of the code.
         */
        for (Block e : PowerUtils.getSurroundingBlocksFarm(blockFace, block, Reference.RADIUS)) {
            final Material blockMat = e.getType();

            // Check if player has permission to break the block
            if (!PowerUtils.canBreak(plugin, player, e)) {
                continue;
            }

            if (PowerUtils.validatePlow(handItem.getType(), blockMat)) {
                debuggingMessages.sendConsoleMessage(ChatColor.RED + "Tilling: " + e.getType());
                usePowerTool(player, handItem, e, Material.FARMLAND);
                continue;
            }

            if (PowerUtils.validatePath(handItem.getType(), blockMat)) {
                usePowerTool(player, handItem, e, Material.DIRT_PATH);
            }
        }

        if (!useDurabilityPerBlock && player.getGameMode().equals(GameMode.SURVIVAL)) {
            PowerUtils.reduceDurability(player, handItem);
        }

    }

    /**
     * Replaces the block with a new material and reduces tool durability.
     * 
     * @param player   The {@link Player} using the PowerTool.
     * @param handItem The {@link ItemStack} representing the tool being used.
     * @param block    The {@link Block} being modified.
     * @param material The {@link Material} to replace the block with.
     */
    private void usePowerTool(@Nonnull final Player player, @Nonnull final ItemStack handItem,
            @Nonnull final Block block, @Nonnull final Material material) {
        block.setType(material);
        // Reduce durability for each block
        if (useDurabilityPerBlock && player.getGameMode().equals(GameMode.SURVIVAL)) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Performs basic verifications before applying PowerTool mechanics.
     * 
     * <p>
     * Ensures the interaction is valid, the player is using a PowerTool, and
     * the block is tillable or pathable.
     * </p>
     * 
     * @param action       The {@link Action} performed by the player.
     * @param player       The {@link Player} who performed the action.
     * @param handItem     The {@link ItemStack} held by the player.
     * @param handItemType The {@link Material} type of the held item.
     * @param block        The {@link Block} clicked by the player.
     * @return {@code true} if the interaction should be ignored, {@code false} if
     *         valid.
     */
    private boolean basicVerifications(@Nonnull final Action action, @Nonnull final Player player,
            @Nonnull final ItemStack handItem, @Nonnull final Material handItemType, @Nullable final Block block) {
        if (action == Action.LEFT_CLICK_BLOCK) {
            return true;
        }
        if (action == Action.LEFT_CLICK_AIR) {
            return true;
        }
        if (action == Action.RIGHT_CLICK_AIR) {
            return true;
        }
        if (player.isSneaking()) {
            return true;
        }
        if (handItem.getType().equals(Material.AIR)) {
            return true;
        }
        if (block == null) {
            return true;
        }
        if (!PowerUtils.isTillable(block.getType())) {
            return true;
        }
        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }
        if (!PowerUtils.checkUsePermission(plugin, player, handItemType)) {
            return true;
        }

        return false;
    }
}

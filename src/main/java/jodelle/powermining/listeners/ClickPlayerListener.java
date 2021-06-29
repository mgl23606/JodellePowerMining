package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
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

public class ClickPlayerListener implements Listener {
    private final PowerMining plugin;
    private final boolean useDurabilityPerBlock;
    private final DebuggingMessages debuggingMessages;


    public ClickPlayerListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        debuggingMessages = plugin.getDebuggingMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUse(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack handItem = player.getInventory().getItemInMainHand();
        final Material handItemType = handItem.getType();
        final Block block = event.getClickedBlock();

        if (basicVerifications(event, player, handItem, handItemType, block)){
            return;
        }


        final String playerName = player.getName();

        final PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
        final BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

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
                usePowerTool(player, handItem, e, Material.GRASS_PATH);
            }
        }

        if (!useDurabilityPerBlock && player.getGameMode().equals(GameMode.SURVIVAL)){
            PowerUtils.reduceDurability(player, handItem);
        }

    }

    /**
     * Replaces the block and reduces the durability of the tool used
     * @param player Player who used the PowerTool
     * @param handItem Item used by the player
     * @param block Target block
     * @param material Material to replace the target block
     */
    private void usePowerTool(@Nonnull final Player player, @Nonnull final ItemStack handItem, @Nonnull final Block block, @Nonnull final Material material) {
        block.setType(material);
        // Reduce durability for each block
        if (useDurabilityPerBlock && player.getGameMode().equals(GameMode.SURVIVAL)) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    private boolean basicVerifications(@Nonnull final PlayerInteractEvent event, @Nonnull final Player player, @Nonnull final ItemStack handItem, @Nonnull final Material handItemType, @Nullable final Block block) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            return true;
        if (event.getAction() == Action.LEFT_CLICK_AIR)
            return true;
        if (event.getAction() == Action.RIGHT_CLICK_AIR)
            return true;
        if (player.isSneaking())
            return true;
        if(handItem.getType().equals(Material.AIR))
            return true;
        if (block == null){
            return true;
        }
        if (!PowerUtils.isTillable(block.getType())){
            return true;
        }
        if (!PowerUtils.isPowerTool(handItem))
            return true;
        if (!PowerUtils.checkUsePermission(player, handItemType))
            return true;

        return false;
    }
}

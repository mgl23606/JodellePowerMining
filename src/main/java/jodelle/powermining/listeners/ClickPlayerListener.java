package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ClickPlayerListener implements Listener
{
    public PowerMining plugin;
    public boolean useDurabilityPerBlock;


    public ClickPlayerListener(PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    @EventHandler(priority= EventPriority.HIGH)
    public void onPlayerUse(PlayerInteractEvent event){
        Player player = event.getPlayer();
        ItemStack handItem = player.getItemInHand();
        Material handItemType = handItem.getType();
        try {
            if (player != null && (player instanceof Player)) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK)
                    return;
                if (event.getAction() == Action.LEFT_CLICK_AIR)
                    return;
                if (player.isSneaking())
                    return;
                if (!PowerUtils.checkUsePermission(player, handItemType))
                    return;
                if (!PowerUtils.isPowerTool(handItem))
                    return;

                Block block = event.getClickedBlock();
                String playerName = player.getName();

                PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
                BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);

                short curDur = handItem.getDurability();
                short maxDur = handItem.getType().getMaxDurability();

                for (Block e: PowerUtils.getSurroundingBlocksFarm(blockFace, block, Reference.HOE_RADIUS)) {
                    Material blockMat = e.getType();
                    Location blockLoc = e.getLocation();

                    boolean useHoe = false;
                    if (useHoe = PowerUtils.validateHoe(handItem.getType(),blockMat));
                    if (useHoe){
                        //e.breakNaturally(handItem);
                        e.setType(Material.FARMLAND);
                        // If this is set, durability will be reduced from the tool for each broken block
                        if (useDurabilityPerBlock || !player.hasPermission("powermining.highdurability")) {
                            if (curDur++ < maxDur)
                                handItem.setDurability(curDur);
                            else
                                break;
                        }
                    }
                }
            }
        } catch (Exception e ){ }
    }
}

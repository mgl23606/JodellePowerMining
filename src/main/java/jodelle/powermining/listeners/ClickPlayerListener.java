package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.PowerUtils;
import jodelle.powermining.lib.Reference;
import org.bukkit.*;
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

public class ClickPlayerListener implements Listener {
    public PowerMining plugin;
    public boolean useDurabilityPerBlock;
    private final DebuggingMessages debuggingMessages;


    public ClickPlayerListener(PowerMining plugin) {
        this.plugin = plugin;
        debuggingMessages = plugin.getDebuggingMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        useDurabilityPerBlock = plugin.getConfig().getBoolean("useDurabilityPerBlock");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        Material handItemType = handItem.getType();
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            return;
        if (event.getAction() == Action.LEFT_CLICK_AIR)
            return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR)
            return;
        if (player.isSneaking())
            return;
        if(handItem.getType().equals(Material.AIR))
            return;
        if (block == null){
            return;
        }
        if (!PowerUtils.isFarm(block.getType())){
            return;
        }
        if (!PowerUtils.isPowerTool(handItem))
            return;
        if (!PowerUtils.checkUsePermission(player, handItemType))
            return;



        String playerName = player.getName();

        PlayerInteractListener pil = plugin.getPlayerInteractHandler().getListener();
        BlockFace blockFace = pil.getBlockFaceByPlayerName(playerName);


        for (Block e : PowerUtils.getSurroundingBlocksFarm(blockFace, block, Reference.RADIUS)) {
            Material blockMat = e.getType();
            console.sendMessage(ChatColor.RED + "[JodellePowerMining] block: " + e.getType());

            // Check if player has permission to break the block
            if (!PowerUtils.canBreak(plugin, player, e)) {
                continue;
            }

            boolean usePlow;
            boolean usePath = false;

            if (usePlow = PowerUtils.validatePlow(handItem.getType(), blockMat)) ;
            else if (usePath = PowerUtils.validatePath(handItem.getType(), blockMat)) ;

            if (usePlow) {
                debuggingMessages.sendConsoleMessage(ChatColor.RED + "Tilling: " + e.getType());

                e.setType(Material.FARMLAND);
                // Reduce durability for each block
                if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                    PowerUtils.reduceDurability(player, handItem);
                }
                continue;
            }

            if (usePath) {
                e.setType(Material.GRASS_PATH);

                // Reduce durability for each block
                if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                    PowerUtils.reduceDurability(player, handItem);
                }
            }
        }

    }
}

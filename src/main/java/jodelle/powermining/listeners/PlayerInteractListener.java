package jodelle.powermining.listeners;

import java.util.HashMap;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;

public class PlayerInteractListener implements Listener {
    private final PowerMining plugin;
    private final HashMap<String, BlockFace> faces = new HashMap<>();
    private final DebuggingMessages debuggingMessages;

	public PlayerInteractListener(@Nonnull PowerMining plugin) {
        debuggingMessages = plugin.getDebuggingMessages();		
        this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		// Example usage: Log to confirm the listener is being registered
		debuggingMessages.sendConsoleMessage("PlayerInteractListener initialized");
	}

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void saveBlockFace(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BlockFace bf = event.getBlockFace();

        String name = player.getName();
        faces.put(name, bf);
    }

    public BlockFace getBlockFaceByPlayerName(@Nonnull final String name) {
        return faces.get(name);
    }
}

/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Tracks the BlockFace a player interacts with, used to determine block-breaking direction.
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInteractListener implements Listener {

    private final PowerMining plugin;

    // Thread-safe since multiple players can interact concurrently
    private final Map<String, BlockFace> faces = new ConcurrentHashMap<>();

    public PlayerInteractListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Stores the BlockFace of the last interacted block for each player.
     *
     * @param event Player interaction event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(@Nonnull final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final BlockFace face = event.getBlockFace();

        if (face != null) {
            faces.put(player.getName(), face);
        }
    }

    /**
     * Gets the last recorded BlockFace for a player.
     *
     * @param playerName The name of the player
     * @return The last BlockFace the player interacted with, or null if none recorded
     */
    public BlockFace getBlockFaceByPlayerName(@Nonnull final String playerName) {
        return faces.get(playerName);
    }
}

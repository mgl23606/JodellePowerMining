/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInteractListener implements Listener {

    private final PowerMining plugin;

    // Stores the last block face each player interacted with.
    // Using UUIDs instead of names prevents memory leaks and handles name changes safely.
    private final Map<UUID, BlockFace> faces = new ConcurrentHashMap<>();

    public PlayerInteractListener(@NotNull final PowerMining plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Listens for player interactions to record the exact face of the block they clicked.
     * This tells our custom tools (hammers/excavators) which direction to expand the break area.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(@NotNull final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final BlockFace face = event.getBlockFace();

        if (face != null) {
            faces.put(player.getUniqueId(), face);
        }
    }

    /**
     * Gets the last recorded BlockFace for a player using their UUID.
     */
    @Nullable
    public BlockFace getBlockFaceByPlayer(@NotNull final UUID playerUuid) {
        return faces.get(playerUuid);
    }

    /**
     * Legacy lookup method maintained for compatibility with other handlers.
     */
    @Nullable
    public BlockFace getBlockFaceByPlayerName(@NotNull final String playerName) {
        Player player = plugin.getServer().getPlayerExact(playerName);
        if (player != null) {
            return faces.get(player.getUniqueId());
        }
        return null;
    }
}
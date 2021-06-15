/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * This class is responsible for getting the BlockFace from which the player is breaking the block
 */

package jodelle.powermining.listeners;

import java.util.HashMap;

import jodelle.powermining.PowerMining;
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

	public PlayerInteractListener(@Nonnull PowerMining plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

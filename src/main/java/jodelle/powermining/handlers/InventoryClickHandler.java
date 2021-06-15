/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Handler class for the InventoryClickEvent Listener, used to create the listener and keep a reference to it
 */

package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.InventoryClickListener;

import javax.annotation.Nonnull;

public class InventoryClickHandler {
	public InventoryClickHandler() {}
	public InventoryClickListener listener;

	public void Init(@Nonnull PowerMining plugin) {
		listener = new InventoryClickListener(plugin);
	}

	@Nonnull
	public InventoryClickListener getListener() {
		return listener;
	}
}

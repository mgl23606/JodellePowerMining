/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Handler class for the CraftItemHammer/CraftItemExcavator classes, used to create the instances and keep a reference to them
 */

package jodelle.powermining.handlers;


import jodelle.powermining.PowerMining;
import jodelle.powermining.crafting.CraftItemExcavator;
import jodelle.powermining.crafting.CraftItemHammer;
import jodelle.powermining.crafting.CraftItemPlow;
import jodelle.powermining.listeners.CraftItemListener;

import javax.annotation.Nonnull;

public class CraftItemHandler {
	public CraftItemHandler() {}
	public CraftItemHammer HammerClass;
	public CraftItemExcavator ExcavatorClass;
	public CraftItemPlow HoeClass;
	public CraftItemListener listener;

	public void Init(@Nonnull PowerMining plugin) {
		HammerClass = new CraftItemHammer(plugin);
		ExcavatorClass = new CraftItemExcavator(plugin);
		HoeClass = new CraftItemPlow(plugin);
		listener = new CraftItemListener(plugin);
	}

	@Nonnull
	public CraftItemListener getListener() {
		return listener;
	}
}
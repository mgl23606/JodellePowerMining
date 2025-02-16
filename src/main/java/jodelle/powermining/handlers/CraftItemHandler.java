package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.crafting.CraftItemExcavator;
import jodelle.powermining.crafting.CraftItemHammer;
import jodelle.powermining.crafting.CraftItemPlow;
import jodelle.powermining.listeners.CraftItemListener;

import javax.annotation.Nonnull;

/**
 * Handles the initialization and management of crafting-related classes and listeners.
 * 
 * <p>
 * This class is responsible for creating and maintaining references to instances of 
 * {@link CraftItemHammer}, {@link CraftItemExcavator}, {@link CraftItemPlow}, and 
 * {@link CraftItemListener} within the PowerMining plugin.
 * </p>
 */
public class CraftItemHandler {
    /**
     * Constructs an instance of {@code CraftItemHandler}.
     */
	public CraftItemHandler() {}
	
	public CraftItemHammer HammerClass;
	public CraftItemExcavator ExcavatorClass;
	public CraftItemPlow HoeClass;
	public CraftItemListener listener;

    /**
     * Initializes crafting-related classes and registers the crafting event listener.
     * 
     * <p>
     * This method creates instances of {@link CraftItemHammer}, {@link CraftItemExcavator}, 
     * and {@link CraftItemPlow} to manage the crafting process of different tool types. 
     * Additionally, it initializes a {@link CraftItemListener} to handle crafting events.
     * </p>
     * 
     * @param plugin The instance of {@link PowerMining} used to initialize crafting classes 
     *               and register event listeners.
     */
	public void Init(@Nonnull PowerMining plugin) {
		HammerClass = new CraftItemHammer(plugin);
		ExcavatorClass = new CraftItemExcavator(plugin);
		HoeClass = new CraftItemPlow(plugin);
		listener = new CraftItemListener(plugin);
	}

    /**
     * Retrieves the crafting event listener instance.
     * 
     * @return The {@link CraftItemListener} associated with this handler.
     */
	@Nonnull
	public CraftItemListener getListener() {
		return listener;
	}
}
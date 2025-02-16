package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.InventoryClickListener;

import javax.annotation.Nonnull;

/**
 * Handles the initialization and management of the {@link InventoryClickListener}.
 * 
 * <p>
 * This class is responsible for creating and maintaining a reference to the 
 * inventory click event listener used in the PowerMining plugin.
 * </p>
 */
public class InventoryClickHandler {
    /**
     * Constructs an instance of {@code InventoryClickHandler}.
     */
	public InventoryClickHandler() {}
	public InventoryClickListener listener;

    /**
     * Initializes the {@link InventoryClickListener} with the given plugin instance.
     * 
     * @param plugin The instance of {@link PowerMining} used to register event listeners.
     */
	public void Init(@Nonnull PowerMining plugin) {
		listener = new InventoryClickListener(plugin);
	}

    /**
     * Retrieves the inventory click event listener instance.
     * 
     * @return The {@link InventoryClickListener} associated with this handler.
     */
	@Nonnull
	public InventoryClickListener getListener() {
		return listener;
	}
}

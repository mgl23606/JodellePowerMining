package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.PlayerInteractListener;

import javax.annotation.Nonnull;

/**
 * Handles the initialization and management of the
 * {@link PlayerInteractListener}.
 * 
 * <p>
 * This class is responsible for creating and maintaining a reference to the
 * player interaction event listener used in the PowerMining plugin.
 * </p>
 */
public class PlayerInteractHandler {
	/**
	 * Constructs an instance of {@code PlayerInteractHandler}.
	 */
	public PlayerInteractHandler() {
	}

	public PlayerInteractListener listener;

	/**
	 * Initializes the {@link PlayerInteractListener} with the given plugin
	 * instance.
	 * 
	 * @param plugin The instance of {@link PowerMining} used to register event
	 *               listeners.
	 */
	public void Init(@Nonnull PowerMining plugin) {
		listener = new PlayerInteractListener(plugin);
	}

	/**
	 * Retrieves the player interaction event listener instance.
	 * 
	 * @return The {@link PlayerInteractListener} associated with this handler.
	 */
	@Nonnull
	public PlayerInteractListener getListener() {
		return listener;
	}
}

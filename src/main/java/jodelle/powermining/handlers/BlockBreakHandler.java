/*
 * Handler class for the BlockBreakEvent Listener, used to create the listener and keep a reference to it
 */

package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.BlockBreakListener;

import javax.annotation.Nonnull;

/**
 * Handles the initialization and management of the {@link BlockBreakListener}.
 * 
 * <p>
 * This class is responsible for creating and maintaining a reference to the
 * block break event listener used in the PowerMining plugin.
 * </p>
 */
public class BlockBreakHandler {
	/**
	 * Constructs an instance of {@code BlockBreakHandler}.
	 */
	public BlockBreakHandler() {
	}

	public BlockBreakListener listener;

	/**
	 * Initializes the {@link BlockBreakListener} with the given plugin instance.
	 * 
	 * @param plugin The instance of {@link PowerMining} used to register event
	 *               listeners.
	 */
	public void Init(@Nonnull PowerMining plugin) {
		listener = new BlockBreakListener(plugin);
	}

	/**
	 * Retrieves the block break event listener instance.
	 * 
	 * @return The {@link BlockBreakListener} associated with this handler.
	 */
	@Nonnull
	public BlockBreakListener getListener() {
		return listener;
	}
}

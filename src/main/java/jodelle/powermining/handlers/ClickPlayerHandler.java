package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.ClickPlayerListener;

import javax.annotation.Nonnull;

/**
 * Handles the initialization and management of the {@link ClickPlayerListener}.
 * 
 * <p>
 * This class is responsible for creating and maintaining a reference to the
 * player interaction event listener used in the PowerMining plugin.
 * </p>
 */
public class ClickPlayerHandler {

    /**
     * Constructs an instance of {@code ClickPlayerHandler}.
     */
    public ClickPlayerHandler() {
    }

    public ClickPlayerListener listener;

    /**
     * Initializes the {@link ClickPlayerListener} with the given plugin instance.
     * 
     * @param plugin The instance of {@link PowerMining} used to register event
     *               listeners.
     */
    public void Init(@Nonnull PowerMining plugin) {
        listener = new ClickPlayerListener(plugin);
    }

    /**
     * Retrieves the player interaction event listener instance.
     * 
     * @return The {@link ClickPlayerListener} associated with this handler.
     */
    @Nonnull
    public ClickPlayerListener getListener() {
        return listener;
    }
}

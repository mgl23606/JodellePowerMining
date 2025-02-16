package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.AnvilRepairListener;

import javax.annotation.Nonnull;

/**
 * Handles the initialization and management of the {@link AnvilRepairListener}.
 * 
 * <p>
 * This class is responsible for setting up the anvil repair event listener
 * within the PowerMining plugin.
 * </p>
 */
public class AnvilRepairHandler {
    /**
     * Constructs an instance of {@code AnvilRepairHandler}.
     */
    public AnvilRepairHandler() {
    }

    private AnvilRepairListener listener;

    /**
     * Initializes the {@link AnvilRepairListener} with the given plugin instance.
     * 
     * @param plugin The instance of {@link PowerMining} used to register event
     *               listeners.
     */
    public void Init(@Nonnull PowerMining plugin) {
        listener = new AnvilRepairListener(plugin);
    }

    /**
     * Retrieves the anvil repair event listener instance.
     * 
     * @return The {@link AnvilRepairListener} associated with this handler.
     */
    @Nonnull
    public AnvilRepairListener getListener() {
        return listener;
    }
}

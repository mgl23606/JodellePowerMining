package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.AnvilRepairListener;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Handles the initialization and management of the {@link AnvilRepairListener}.
 *
 * <p>
 * This class is responsible for setting up the anvil repair event listener
 * within the PowerMining plugin.
 * </p>
 *
 * <p>
 * The listener is initialized via {@link #init(PowerMining)}. Calling {@link #getListener()}
 * before initialization will result in an exception.
 * </p>
 */
public class AnvilRepairHandler {

    private AnvilRepairListener listener;

    /**
     * Constructs an instance of {@code AnvilRepairHandler}.
     */
    public AnvilRepairHandler() {
    }

    /**
     * Initializes the {@link AnvilRepairListener} with the given plugin instance.
     *
     * @param plugin The instance of {@link PowerMining} used to create the listener.
     * @throws NullPointerException if {@code plugin} is null
     */
    public void init(@Nonnull PowerMining plugin) {
        // Make initialization explicit and keep it non-null from this point forward
        this.listener = new AnvilRepairListener(Objects.requireNonNull(plugin, "plugin"));
    }

    /**
     * Retrieves the anvil repair event listener instance.
     *
     * @return The {@link AnvilRepairListener} associated with this handler.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public AnvilRepairListener getListener() {
        // Make nullness explicit for the compiler/null analysis
        return Objects.requireNonNull(listener, "AnvilRepairHandler has not been initialized yet");
    }
}

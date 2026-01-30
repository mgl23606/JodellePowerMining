package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.ClickPlayerListener;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Handles the initialization and management of the {@link ClickPlayerListener}.
 *
 * <p>
 * This class is responsible for creating and maintaining a reference to the
 * player interaction event listener used in the PowerMining plugin.
 * </p>
 *
 * <p>
 * The listener is initialized via {@link #init(PowerMining)}. Calling
 * {@link #getListener()} before initialization will result in an exception.
 * </p>
 */
public class ClickPlayerHandler {

    private ClickPlayerListener listener;

    /**
     * Constructs an instance of {@code ClickPlayerHandler}.
     */
    public ClickPlayerHandler() {
    }

    /**
     * Initializes the {@link ClickPlayerListener} with the given plugin instance.
     *
     * @param plugin The instance of {@link PowerMining} used to create the listener.
     * @throws NullPointerException if {@code plugin} is null
     */
    public void init(@Nonnull PowerMining plugin) {
        // Make initialization explicit and keep it non-null from this point forward
        this.listener = new ClickPlayerListener(Objects.requireNonNull(plugin, "plugin"));
    }

    /**
     * Retrieves the player interaction event listener instance.
     *
     * @return The {@link ClickPlayerListener} associated with this handler.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public ClickPlayerListener getListener() {
        // Make nullness explicit for the compiler/null analysis
        return Objects.requireNonNull(listener, "ClickPlayerHandler has not been initialized yet");
    }
}
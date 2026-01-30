package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.PlayerInteractListener;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Handles the initialization and management of the {@link PlayerInteractListener}.
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
public class PlayerInteractHandler {

    private PlayerInteractListener listener;

    /**
     * Constructs an instance of {@code PlayerInteractHandler}.
     */
    public PlayerInteractHandler() {
    }

    /**
     * Initializes the {@link PlayerInteractListener} with the given plugin instance.
     *
     * @param plugin The instance of {@link PowerMining} used to create the listener.
     * @throws NullPointerException if {@code plugin} is null
     */
    public void init(@Nonnull PowerMining plugin) {
        // Make initialization explicit and keep it non-null from this point forward
        this.listener = new PlayerInteractListener(Objects.requireNonNull(plugin, "plugin"));
    }

    /**
     * Retrieves the player interaction event listener instance.
     *
     * @return The {@link PlayerInteractListener} associated with this handler.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public PlayerInteractListener getListener() {
        // Make nullness explicit for the compiler/null analysis
        return Objects.requireNonNull(listener, "PlayerInteractHandler has not been initialized yet");
    }
}

package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.InventoryClickListener;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Handles the initialization and management of the {@link InventoryClickListener}.
 *
 * <p>
 * This class is responsible for creating and maintaining a reference to the
 * inventory click event listener used in the PowerMining plugin.
 * </p>
 *
 * <p>
 * The listener is initialized via {@link #init(PowerMining)}. Calling
 * {@link #getListener()} before initialization will result in an exception.
 * </p>
 */
public class InventoryClickHandler {

    private InventoryClickListener listener;

    /**
     * Constructs an instance of {@code InventoryClickHandler}.
     */
    public InventoryClickHandler() {
    }

    /**
     * Initializes the {@link InventoryClickListener} with the given plugin instance.
     *
     * @param plugin The instance of {@link PowerMining} used to create the listener.
     * @throws NullPointerException if {@code plugin} is null
     */
    public void init(@Nonnull PowerMining plugin) {
        // Make initialization explicit and keep it non-null from this point forward
        this.listener = new InventoryClickListener(Objects.requireNonNull(plugin, "plugin"));
    }

    /**
     * Retrieves the inventory click event listener instance.
     *
     * @return The {@link InventoryClickListener} associated with this handler.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public InventoryClickListener getListener() {
        // Make nullness explicit for the compiler/null analysis
        return Objects.requireNonNull(listener, "InventoryClickHandler has not been initialized yet");
    }
}

package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.crafting.CraftItemExcavator;
import jodelle.powermining.crafting.CraftItemHammer;
import jodelle.powermining.crafting.CraftItemPlow;
import jodelle.powermining.listeners.CraftItemListener;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Handles the initialization and management of crafting-related classes and listeners.
 *
 * <p>
 * This class is responsible for creating and maintaining references to instances of
 * {@link CraftItemHammer}, {@link CraftItemExcavator}, {@link CraftItemPlow}, and
 * {@link CraftItemListener} within the PowerMining plugin.
 * </p>
 *
 * <p>
 * All crafting-related components are initialized via {@link #init(PowerMining)}.
 * Calling any getter before initialization will result in an exception.
 * </p>
 */
public class CraftItemHandler {

    private CraftItemHammer hammerClass;
    private CraftItemExcavator excavatorClass;
    private CraftItemPlow plowClass;
    private CraftItemListener listener;

    /**
     * Constructs an instance of {@code CraftItemHandler}.
     */
    public CraftItemHandler() {
    }

    /**
     * Initializes crafting-related classes and the crafting event listener.
     *
     * <p>
     * This method creates instances of {@link CraftItemHammer}, {@link CraftItemExcavator},
     * and {@link CraftItemPlow} to manage the crafting process of different tool types.
     * Additionally, it initializes a {@link CraftItemListener} to handle crafting events.
     * </p>
     *
     * @param plugin The instance of {@link PowerMining} used to initialize crafting classes
     *               and listeners.
     * @throws NullPointerException if {@code plugin} is null
     */
    public void init(@Nonnull PowerMining plugin) {
        final PowerMining nonNullPlugin = Objects.requireNonNull(plugin, "plugin");

        this.hammerClass = new CraftItemHammer(nonNullPlugin);
        this.excavatorClass = new CraftItemExcavator(nonNullPlugin);
        this.plowClass = new CraftItemPlow(nonNullPlugin);
        this.listener = new CraftItemListener(nonNullPlugin);
    }

    /**
     * Retrieves the crafting event listener instance.
     *
     * @return The {@link CraftItemListener} associated with this handler.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public CraftItemListener getListener() {
        return Objects.requireNonNull(listener, "CraftItemHandler has not been initialized yet");
    }

    /**
     * Retrieves the hammer crafting handler.
     *
     * @return The {@link CraftItemHammer} instance.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public CraftItemHammer getHammerClass() {
        return Objects.requireNonNull(hammerClass, "CraftItemHandler has not been initialized yet");
    }

    /**
     * Retrieves the excavator crafting handler.
     *
     * @return The {@link CraftItemExcavator} instance.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public CraftItemExcavator getExcavatorClass() {
        return Objects.requireNonNull(excavatorClass, "CraftItemHandler has not been initialized yet");
    }

    /**
     * Retrieves the plow crafting handler.
     *
     * @return The {@link CraftItemPlow} instance.
     * @throws IllegalStateException if {@link #init(PowerMining)} has not been called yet
     */
    @Nonnull
    public CraftItemPlow getPlowClass() {
        return Objects.requireNonNull(plowClass, "CraftItemHandler has not been initialized yet");
    }
}
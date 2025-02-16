package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.EnchantItemListener;

import javax.annotation.Nonnull;

/**
 * Handles the initialization and management of the {@link EnchantItemListener}.
 * 
 * <p>
 * This class is responsible for creating and maintaining a reference to the 
 * enchantment event listener used in the PowerMining plugin.
 * </p>
 */
public class EnchantItemHandler {
    /**
     * Constructs an instance of {@code EnchantItemHandler}.
     */
	public EnchantItemHandler() {}
	public EnchantItemListener listener;

    /**
     * Initializes the {@link EnchantItemListener} with the given plugin instance.
     * 
     * @param plugin The instance of {@link PowerMining} used to register event listeners.
     */
	public void Init(@Nonnull PowerMining plugin) {
		listener = new EnchantItemListener(plugin);
	}

    /**
     * Retrieves the enchantment event listener instance.
     * 
     * @return The {@link EnchantItemListener} associated with this handler.
     */
	@Nonnull
	public EnchantItemListener getListener() {
		return listener;
	}
}

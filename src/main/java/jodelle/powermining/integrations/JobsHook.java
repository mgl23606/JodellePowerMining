package jodelle.powermining.integrations;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Interface for integrating with Jobs-related plugins in the PowerMining system.
 * 
 * <p>
 * Implementations of this interface are responsible for notifying external job-based 
 * plugins when a player interacts with or modifies a block, allowing for proper job 
 * tracking and rewards.
 * </p>
 */
public interface JobsHook {
    /**
     * Notifies the job system when a player interacts with a block.
     * 
     * @param player The {@link Player} who performed the action.
     * @param block  The {@link Block} that was affected.
     */
    void notifyJobs(Player player, Block block);
}

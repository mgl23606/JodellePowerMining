package jodelle.powermining.integrations;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Implementation of {@link JobsHook} for integrating with the JobsReborn
 * plugin.
 * 
 * <p>
 * This class notifies the Jobs system when a player breaks a block, ensuring
 * that job-related rewards and tracking are properly handled.
 * </p>
 */
public class JobsHookImpl implements JobsHook {
    /**
     * Notifies the JobsReborn system when a player breaks a block.
     * 
     * <p>
     * This method retrieves the player's job data and triggers a block break
     * action in the Jobs system, allowing job experience and earnings to be
     * calculated.
     * </p>
     * 
     * @param player The {@link Player} who performed the action.
     * @param block  The {@link Block} that was broken.
     */
    @Override
    public void notifyJobs(Player player, Block block) {
        Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), new BlockActionInfo(block, ActionType.BREAK));
    }
}

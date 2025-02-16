package jodelle.powermining.integrations;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class JobsHookImpl implements JobsHook {
    @Override
    public void notifyJobs(Player player, Block block) {
        Jobs.action(Jobs.getPlayerManager().getJobsPlayer(player), new BlockActionInfo(block, ActionType.BREAK));
    }
}

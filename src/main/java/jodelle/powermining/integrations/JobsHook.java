package jodelle.powermining.integrations;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface JobsHook {
    void notifyJobs(Player player, Block block);
}

/*
 * This piece of software is part of the PowerMining Bukkit Plugin
 * Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
 *
 * Licensed under the LGPL v3
 * Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

/*
 * Handles PowerMining commands, including version and item-giving functionality.
 */

package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener, CommandExecutor, TabExecutor {

    private static final String BASE_COMMAND = "jpm";
    private final PowerMining plugin;

    public CommandListener(@Nonnull final PowerMining plugin) {
        this.plugin = plugin;
        final PluginCommand pluginCommand = plugin.getCommand(BASE_COMMAND);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(
            @Nonnull final CommandSender sender,
            @Nonnull final Command command,
            @Nonnull final String label,
            @Nonnull final String[] args
    ) {
        if (args.length == 0) {
            return sendVersion(sender);
        }

        switch (args[0].toLowerCase()) {
            case "version":
                return sendVersion(sender);

            case "give":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jpm give <toolname>");
                    return true;
                }
                return handleGive(sender, args[1]);

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /jpm version or /jpm give <toolname>");
                return true;
        }
    }

    /**
     * Sends a message showing the plugin version.
     */
    private boolean sendVersion(@Nonnull final CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "[JodellePowerMining] Version: "
                + ChatColor.WHITE + plugin.getDescription().getVersion());
        return true;
    }

    /**
     * Handles the /jpm give command to grant PowerTools to players.
     */
    private boolean handleGive(@Nonnull final CommandSender sender, @Nonnull final String toolName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "[JodellePowerMining] Only players can receive items.");
            return true;
        }

        final Player player = (Player) sender;

        if (!player.hasPermission("powermining.give")) {
            player.sendMessage(ChatColor.RED + "[JodellePowerMining] You don't have permission to use this command.");
            return true;
        }

        final NamespacedKey key = new NamespacedKey(plugin, toolName.toUpperCase());
        final Recipe recipe = plugin.getServer().getRecipe(key);

        if (recipe == null) {
            player.sendMessage(ChatColor.RED + "[JodellePowerMining] PowerTool not found: " + toolName);
            return true;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "[JodellePowerMining] Your inventory is full!");
            return true;
        }

        player.getInventory().addItem(recipe.getResult());
        player.sendMessage(ChatColor.GREEN + "[JodellePowerMining] You have received a " + toolName + "!");
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @Nonnull final CommandSender sender,
            @Nonnull final Command command,
            @Nonnull final String alias,
            @Nonnull final String[] args
    ) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("version");
            completions.add("give");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Reference.EXCAVATORS);
            completions.addAll(Reference.HAMMERS);
            completions.addAll(Reference.PLOWS);
            return completions;
        }

        return completions;
    }
}

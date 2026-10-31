/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener, CommandExecutor, TabExecutor {

    private static final String BASE_COMMAND = "jpm";
    private final PowerMining plugin;

    public CommandListener(@NotNull final PowerMining plugin) {
        this.plugin = plugin;
        final PluginCommand pluginCommand = plugin.getCommand(BASE_COMMAND);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String label,
            @NotNull final String[] args
    ) {
        // If they just type /jpm without any args, default to showing version info
        if (args.length == 0) {
            return sendVersion(sender);
        }

        // Route subcommands cleanly using a switch statement
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
     * Sends a chat message displaying the current active version of the plugin.
     */
    private boolean sendVersion(@NotNull final CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "[JodellePowerMining] Version: "
                + ChatColor.WHITE + plugin.getDescription().getVersion());
        return true;
    }

    /**
     * Handles the /jpm give command to spawn custom power tools directly to authorized players.
     */
    private boolean handleGive(@NotNull final CommandSender sender, @NotNull final String toolName) {
        // Make sure the command runner is an actual in-game player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "[JodellePowerMining] Only players can receive items.");
            return true;
        }

        // Check if they have permission to use the give command
        if (!player.hasPermission("powermining.give")) {
            player.sendMessage(ChatColor.RED + "[JodellePowerMining] You don't have permission to use this command.");
            return true;
        }

        // Look up the requested tool recipe by its namespaced key
        final NamespacedKey key = new NamespacedKey(plugin, toolName.toUpperCase());
        final Recipe recipe = plugin.getServer().getRecipe(key);

        if (recipe == null) {
            player.sendMessage(ChatColor.RED + "[JodellePowerMining] PowerTool not found: " + toolName);
            return true;
        }

        // Verify they have an open slot in their inventory
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "[JodellePowerMining] Your inventory is full!");
            return true;
        }

        // Deliver the custom tool item and send a confirmation message
        player.getInventory().addItem(recipe.getResult());
        player.sendMessage(ChatColor.GREEN + "[JodellePowerMining] You have received a " + toolName + "!");
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String alias,
            @NotNull final String[] args
    ) {
        final List<String> completions = new ArrayList<>();

        // Provide subcommand suggestions for the first argument
        if (args.length == 1) {
            completions.add("version");
            completions.add("give");
            return completions;
        }

        // Provide specific tool name suggestions if they are typing /jpm give
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Reference.EXCAVATORS);
            completions.addAll(Reference.HAMMERS);
            completions.addAll(Reference.PLOWS);
            return completions;
        }

        return completions;
    }
}
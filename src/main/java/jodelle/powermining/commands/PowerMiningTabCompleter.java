package jodelle.powermining.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import jodelle.powermining.lib.Reference;
import jodelle.powermining.PowerMining;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles tab completion for the `/powermining` command.
 * 
 * <p>
 * Provides suggestions for various subcommands and arguments to enhance user
 * experience. It supports:
 * </p>
 * <ul>
 * <li>Main command suggestions (e.g., `give`, `admin`, `version`, `help`,
 * `info`).</li>
 * <li>Admin subcommands (e.g., `reload`, `language`, `debug`, `help`).</li>
 * <li>PowerTools suggestions for the `give` command (Excavators, Hammers,
 * Plows).</li>
 * <li>Available language options for `/powermining admin language`.</li>
 * </ul>
 */
public class PowerMiningTabCompleter implements TabCompleter {
    private final List<String> availableLanguages;

    public PowerMiningTabCompleter(PowerMining plugin) {
        availableLanguages = plugin.getAvailableLanguagesFromCache();
    }

    /**
     * Provides tab-completion suggestions for the `/powermining` command.
     * 
     * <p>
     * Depending on the command arguments, it suggests:
     * </p>
     * <ul>
     * <li>Level 1: Main command keywords (`give`, `admin`, `version`, `help`,
     * `info`).</li>
     * <li>Level 2: Subcommands (`reload`, `language`, `debug`, `help` for
     * `admin`).</li>
     * <li>Level 2: PowerTools list when using `/powermining give`.</li>
     * <li>Level 3: Available language options when using `/powermining admin
     * language`.</li>
     * </ul>
     * 
     * @param sender The sender of the command (player or console).
     * @param cmd    The command being executed.
     * @param alias  The alias of the command.
     * @param args   The arguments typed so far.
     * @return A list of possible completions based on the current input.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main command suggestions
            completions.add("give");
            completions.add("admin");
            completions.add("version");
            completions.add("help");
            completions.add("info");
        } else if (args.length == 2) {
            // Subcommands
            if ("admin".equalsIgnoreCase(args[0])) {
                completions.add("reload");
                completions.add("language");
                completions.add("debug");
                completions.add("help");
            } else if ("give".equalsIgnoreCase(args[0])) {
                completions.addAll(Reference.EXCAVATORS);
                completions.addAll(Reference.HAMMERS);
                completions.addAll(Reference.PLOWS);
            }
        } else if (args.length == 3 && "admin".equalsIgnoreCase(args[0]) && "language".equalsIgnoreCase(args[1])) {
            // Suggest preloaded languages, but allow user input
            completions.addAll(availableLanguages);
            completions.add("<custom_language>"); // Placeholder to indicate user can input their own
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}

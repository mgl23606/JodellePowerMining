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
 * Handles tab completion for the {@code /powermining} command.
 *
 * <p>
 * This class provides context-aware tab completion suggestions for players
 * and the console to improve usability and reduce typing effort.
 * </p>
 *
 * <p>
 * Supported completions include:
 * </p>
 * <ul>
 * <li>Main command keywords (e.g. {@code give}, {@code admin}, {@code version},
 * {@code help}, {@code info})</li>
 * <li>Administrative subcommands (e.g. {@code reload}, {@code language},
 * {@code debug}, {@code help})</li>
 * <li>Available PowerTools for the {@code give} command (Excavators,
 * Hammers, Plows)</li>
 * <li>Available language options for {@code /powermining admin language}</li>
 * </ul>
 */
public class PowerMiningTabCompleter implements TabCompleter {
    /**
     * Cached list of available language identifiers.
     *
     * <p>
     * This list is populated once during construction to avoid repeated
     * lookups during tab completion.
     * </p>
     */
    private final List<String> availableLanguages;

    /**
     * Creates a new {@code PowerMiningTabCompleter}.
     *
     * <p>
     * The constructor retrieves and caches the available languages from the
     * plugin instance to be used for tab completion.
     * </p>
     *
     * @param plugin The main {@link PowerMining} plugin instance.
     */
    public PowerMiningTabCompleter(PowerMining plugin) {
        availableLanguages = plugin.getAvailableLanguagesFromCache();
    }

    /**
     * Provides tab-completion suggestions for the {@code /powermining} command.
     *
     * <p>
     * Suggestions depend on the current argument depth:
     * </p>
     * <ul>
     * <li>Argument 1: Main command keywords</li>
     * <li>Argument 2: Subcommands or PowerTool names</li>
     * <li>Argument 3: Language identifiers for the admin language command</li>
     * </ul>
     *
     * <p>
     * All suggestions are filtered based on the current user input
     * (case-insensitive prefix matching).
     * </p>
     *
     * @param sender The sender of the command (player or console).
     * @param cmd    The command being executed.
     * @param alias  The alias used to execute the command.
     * @param args   The arguments entered so far.
     * @return A list of possible tab-completion suggestions.
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

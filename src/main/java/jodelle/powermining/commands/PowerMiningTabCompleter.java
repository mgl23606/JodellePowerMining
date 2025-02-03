package jodelle.powermining.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import jodelle.powermining.lib.Reference;
import jodelle.powermining.PowerMining;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PowerMiningTabCompleter implements TabCompleter {
    private final List<String> availableLanguages;

    public PowerMiningTabCompleter(PowerMining plugin) {
        availableLanguages = plugin.getAvailableLanguagesFromCache();
    }

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

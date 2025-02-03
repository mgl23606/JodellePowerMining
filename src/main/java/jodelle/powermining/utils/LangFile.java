package jodelle.powermining.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;

import jodelle.powermining.PowerMining;

public class LangFile extends PluginFile {

    public LangFile(PowerMining plugin, String language) {
        super(plugin, "lang/" + language + ".yml");
    }

    public String getMessage(String configPath, String defaultValue, boolean withPrefix) {
        String message = getFileConfig().getString(configPath);
        if (message == null || message.isEmpty()) {
            message = defaultValue;
        }
    
        if (withPrefix) {
            String prefix;
            if (getFileConfig().isString("prefix")) {
                prefix = getFileConfig().getString("prefix");
                // Use the fallback prefix if the prefix is null but allow empty strings
                if (prefix == null) {
                    prefix = "&1[&2Jodelle&4Power&6Mining&1]";
                }
            } else {
                // Use the fallback prefix if "prefix" is not a string in the config
                prefix = "&1[&2Jodelle&4Power&6Mining&1]";
            }
    
            if (!prefix.isEmpty()) {
                // Add a space only if the prefix does not already end with one
                if (!prefix.endsWith(" ")) {
                    prefix += " ";
                }
                // Append ChatColor.RESET to reset formatting
                prefix += "&r";
            }
    
            message = prefix + message;
        }
    
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public List<String> getMessageList(String configPath) {
        List<String> rawMessages = getFileConfig().getStringList(configPath);
        if (rawMessages == null) {
            return new ArrayList<>();
        }

        List<String> messages = new ArrayList<>();
        for (String message : rawMessages) {
            messages.add(ChatColor.translateAlternateColorCodes('&', message));
        }

        return messages;
    }

    public List<String> getMessageList(String configPath, String... defaultList) {
        List<String> messages = getMessageList(configPath);
        if (messages.isEmpty()) {
            return Arrays.asList(defaultList);
        }

        return messages;
    }
}

package jodelle.powermining.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;

import jodelle.powermining.PowerMining;

/**
 * Handles language file operations for retrieving localized messages in the PowerMining plugin.
 * 
 * <p>
 * This class extends {@link PluginFile} to provide methods for fetching messages and lists of messages
 * from language configuration files. It supports default values, prefixes, and color code translation.
 * </p>
 */
public class LangFile extends PluginFile {

    /**
     * Constructs a {@code LangFile} instance for a specified language file.
     * 
     * @param plugin   The instance of {@link PowerMining} responsible for managing the language file.
     * @param language The language code used to load the appropriate YAML file (e.g., "en_US").
     */
    public LangFile(PowerMining plugin, String language) {
        super(plugin, "lang/" + language + ".yml");
    }

    /**
     * Retrieves a localized message from the language file, applying color codes and an optional prefix.
     * 
     * <p>
     * If the message is missing or empty, the provided default value is returned.
     * If {@code withPrefix} is set to {@code true}, the message is prefixed with a customizable plugin prefix.
     * </p>
     * 
     * @param configPath   The path to the message in the language configuration file.
     * @param defaultValue The default message to return if the specified message is not found.
     * @param withPrefix   {@code true} if the message should include the plugin's prefix, {@code false} otherwise.
     * @return The formatted message with color codes and an optional prefix.
     */
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

    /**
     * Retrieves a list of localized messages from the language file.
     * 
     * <p>
     * If the specified path does not exist or contains no messages, an empty list is returned.
     * Each message in the list is formatted with color codes.
     * </p>
     * 
     * @param configPath The path to the message list in the language configuration file.
     * @return A {@link List} of formatted messages, or an empty list if none exist.
     */
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

    /**
     * Retrieves a list of localized messages from the language file, returning a default list if none exist.
     * 
     * <p>
     * If the specified path contains no messages, the provided default list is returned instead.
     * Each message in the list is formatted with color codes.
     * </p>
     * 
     * @param configPath  The path to the message list in the language configuration file.
     * @param defaultList The default list of messages to return if none exist in the configuration.
     * @return A {@link List} of formatted messages, or the default list if none exist.
     */
    public List<String> getMessageList(String configPath, String... defaultList) {
        List<String> messages = getMessageList(configPath);
        if (messages.isEmpty()) {
            return Arrays.asList(defaultList);
        }

        return messages;
    }
}

package jodelle.powermining.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import jodelle.powermining.PowerMining;

/**
 * Handles debugging messages for the PowerMining plugin.
 * 
 * <p>
 * This class is responsible for sending debug messages to the console when
 * debug mode is enabled. It checks the plugin's debug mode status and logs
 * messages accordingly.
 * </p>
 */
public class DebuggingMessages {

    protected ConsoleCommandSender console;

    /**
     * Constructs an instance of {@code DebuggingMessages}.
     * 
     * <p>
     * Initializes the console sender and logs an initial debug message if debugging
     * mode is enabled.
     * </p>
     */
    public DebuggingMessages() {
        console = Bukkit.getServer().getConsoleSender();
        if (PowerMining.isDebugMode()) {
            sendConsoleMessage("Debugging is On");
        }
    }

    /**
     * Checks if debugging mode is enabled in the PowerMining plugin.
     * 
     * @return {@code true} if debug mode is enabled, {@code false} otherwise.
     */
    public boolean isDebuggingOn() {
        return PowerMining.isDebugMode();
    }

    /**
     * Sends a debug message to the console if debugging mode is enabled.
     * 
     * <p>
     * Messages are prefixed with "[JodellePowerMiningDebugging]" and displayed in
     * light purple to distinguish them from other logs.
     * </p>
     * 
     * @param message The message to be logged.
     */
    public void sendConsoleMessage(String message) {
        if (isDebuggingOn()) {
            console.sendMessage(ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - " + message);
        }
    }
}
package jodelle.powermining.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import jodelle.powermining.PowerMining;

/**
 * Utility class responsible for handling debug output for the PowerMining plugin.
 *
 * <p>
 * This class provides a centralized way to send debug messages to the server
 * console. All messages are only emitted when the plugin's debug mode is enabled,
 * ensuring that additional logging does not clutter the console during normal
 * operation.
 * </p>
 *
 * <p>
 * Debug messages are visually distinguished by a fixed prefix and a dedicated
 * color, making them easy to identify among other log entries.
 * </p>
 */
public class DebuggingMessages {

    /**
     * Console sender used to output debug messages to the server console.
     */
    protected ConsoleCommandSender console;

    /**
     * Creates a new {@code DebuggingMessages} instance.
     *
     * <p>
     * The constructor initializes the {@link ConsoleCommandSender} and immediately
     * prints a startup message if debug mode is enabled. This helps verifying at
     * server startup that debugging is active.
     * </p>
     */
    public DebuggingMessages() {
        console = Bukkit.getServer().getConsoleSender();
        if (PowerMining.isDebugMode()) {
            sendConsoleMessage("Debugging is On");
        }
    }

    /**
     * Indicates whether debug mode is currently enabled for the plugin.
     *
     * <p>
     * This method directly reflects the global debug configuration of
     * {@link PowerMining}.
     * </p>
     *
     * @return {@code true} if debug mode is enabled, {@code false} otherwise.
     */
    public boolean isDebuggingOn() {
        return PowerMining.isDebugMode();
    }

    /**
     * Sends a formatted debug message to the server console.
     *
     * <p>
     * Messages are only sent if debug mode is enabled. Each message is prefixed
     * with {@code [JodellePowerMiningDebugging]} and colored in
     * {@link ChatColor#LIGHT_PURPLE} to clearly separate debug output from regular
     * server logs.
     * </p>
     *
     * @param message
     *         The message text to log to the console.
     */
    public void sendConsoleMessage(String message) {
        if (isDebuggingOn()) {
            console.sendMessage(ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - " + message);
        }
    }
}
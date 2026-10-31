/*
- This piece of software is part of the PowerMining Bukkit Plugin
- Author: BloodyShade (dev.bukkit.org/profiles/bloodyshade)
- Licensed under the LGPL v3
- Further information please refer to the included lgpl-3.0.txt or the gnu website (http://www.gnu.org/licenses/lgpl)
 */

package jodelle.powermining.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class DebuggingMessages {

    // Master toggle for printing verbose tracking logs to the server console
    protected boolean debuggingOn = false;

    public DebuggingMessages() {
        if (debuggingOn) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - Debugging is On");
        }
    }

    /**
     * Sends a formatted debug message to the console if debugging is enabled.
     */
    public void sendConsoleMessage(@NotNull String message) {
        if (debuggingOn) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - " + message);
        }
    }

    /**
     * Conditional debug message logger that also requires an explicit 'show' boolean check.
     */
    public void sendConsoleMessage(boolean show, @NotNull String message) {
        if (debuggingOn && show) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - " + message);
        }
    }
}
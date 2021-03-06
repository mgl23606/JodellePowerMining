package jodelle.powermining.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class DebuggingMessages {

    protected boolean debugginOn = false;
    protected ConsoleCommandSender console;

    public DebuggingMessages() {
        if (debugginOn){
            console = Bukkit.getServer().getConsoleSender();
            sendConsoleMessage("Debugging is On");
        }
    }

    public void sendConsoleMessage(String message){
        if (debugginOn){
            console.sendMessage( ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - " + message);
        }
    }

    public void sendConsoleMessage(Boolean show, String message){
        if (debugginOn && show){
            console.sendMessage( ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - " + message);
        }
    }

}

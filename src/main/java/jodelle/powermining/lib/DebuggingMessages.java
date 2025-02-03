package jodelle.powermining.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import jodelle.powermining.PowerMining;

public class DebuggingMessages {

    protected ConsoleCommandSender console;

    public DebuggingMessages() {
        console = Bukkit.getServer().getConsoleSender();
        if (PowerMining.isDebugMode()){
            sendConsoleMessage("Debugging is On");
        }
    }
    
    public boolean isDebuggingOn() {
        return PowerMining.isDebugMode();
    }
    
    public void sendConsoleMessage(String message){
        if (isDebuggingOn()){
            console.sendMessage(ChatColor.LIGHT_PURPLE + "[JodellePowerMiningDebugging] - " + message);
        }
    }
}
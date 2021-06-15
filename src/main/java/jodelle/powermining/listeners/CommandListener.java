package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.Reference;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener, CommandExecutor, TabExecutor {

    protected PowerMining plugin;
    protected String command = "jpm";

    public CommandListener(PowerMining plugin) {
        this.plugin = plugin;
        PluginCommand pluginCommand;
        if ((pluginCommand = plugin.getCommand(command)) != null){
            pluginCommand.setExecutor(this);
        }

    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {

        if (args.length == 0){
            return versionCommand(sender);
        }

        switch (args[0]){
            case "version":
                return versionCommand(sender);
            case "give":
                if (args.length < 2){
                    return false;
                }
                return giveCommand(sender, args[1]);
        }


        return true;
    }

    private boolean versionCommand(CommandSender sender) {
        sender.sendMessage("Version: " + plugin.getDescription().getVersion());
        return true;
    }

    /*
    * This method gives a powerTool to a player. The PowerTools are already created and registered
    * as a recipe on the server. This means that using their NameSpacedKey we can access the recipe
    * and consequently the result item. The Key was conveniently chosen to be the name of the PowerTool
    * so it could later be easily found.
    * The method starts by verifying if the sender is a player because it can't be given an item to
    * the console.
    * */

    private boolean giveCommand(CommandSender sender, String powerToolName) {
        if (!(sender instanceof Player)){
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("powermining.give")){
            sender.sendMessage(ChatColor.RED + "[JodellePowerMining] - You don't have permission to use this command");
            return true;
        }

        NamespacedKey namespacedKey = new NamespacedKey(plugin, powerToolName.toUpperCase());

        Recipe recipe = plugin.getServer().getRecipe(namespacedKey);
        if (recipe == null){
            sender.sendMessage(ChatColor.RED + "[JodellePowerMining] - Powertool not found");
            return true;
        }

        if (player.getInventory().firstEmpty() == -1){
            sender.sendMessage(ChatColor.RED + "[JodellePowerMining] - Your inventory is full");
            return true;
        }

        player.getInventory().addItem(recipe.getResult());

        sender.sendMessage(ChatColor.GREEN + "[JodellePowerMining] - " + powerToolName + "Given to " + sender.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete( @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String[] args) {

        List<String> arguments = new ArrayList<>();

        if (args.length == 1){

            arguments.add("version");
            arguments.add("give");
            return arguments;
        }

        if (args.length == 2){
            arguments.addAll(Reference.EXCAVATORS);
            arguments.addAll(Reference.HAMMERS);
            arguments.addAll(Reference.PLOWS);
            return arguments;
        }

        return null;
    }
}

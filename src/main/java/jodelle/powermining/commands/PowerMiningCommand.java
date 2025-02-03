package jodelle.powermining.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Recipe;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.listeners.BlockBreakListener;
import jodelle.powermining.utils.LangFile;

public class PowerMiningCommand implements CommandExecutor {
   private PowerMining plugin;
   private DebuggingMessages debuggingMessages;
   String powerToolName;
   Player p;

   public PowerMiningCommand(PowerMining plugin) {
      this.plugin = plugin;
      debuggingMessages = plugin.getDebuggingMessages();
   }

   public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
      if (cmd.getName().equals("powermining")) {
         if (sender instanceof Player) {
            p = (Player) sender;
         }

         // If arguments (command) is less or same as 0 (only /<command>)
         if (args.length <= 0) {
            sender.sendMessage(plugin.getLangFile().getMessage("plugin-info",
                  "&2Plugin orginally made by &6bloodyshade&2, recreated by &6mgl23606&2, heavily modified and updated by &6Holt&2. See \"&4/powermining help&2\" for a list of subcommands.",
                  true));
            return true;
         }

         // If arguments (command) has 1 argument (/powermining admin)
         else if (args[0].equals("admin") || args[0].equals("adm") || args[0].equals("a")) {
            // If the lenght of the args are exactly one, then send the help for the admin
            // page
            if (args.length > 1) {
               // Command for "/powermining admin reload"
               if (args[1].equalsIgnoreCase("reload")) {
                  if (sender.hasPermission("powermining.admin.reload")) {
                     long startTime = System.currentTimeMillis();

                     // Send message that the reload is starting
                     sender.sendMessage(plugin.getLangFile().getMessage("reloading-plugin",
                           "&cReloading JodellePowerMining...", true));
                     Bukkit.getConsoleSender().sendMessage(plugin.getLangFile().getMessage("reloading-plugin",
                           "&cReloading JodellePowerMining...", true));
                     // Reload Plugin
                     plugin.ensureConfigExists();
                     plugin.ensureLanguageFilesExist();

                     // Unregister recipes - wait for full removal
                     plugin.getLogger().info("Unregistering all recipes...");
                     plugin.unregisterRecipeManager();

                     // Reinitialize LangFile to reload possibly extracted language files (if user
                     // deleted them)
                     String language = plugin.getConfig().getString("language", "en_US");
                     plugin.setLangFile(new LangFile(plugin, language));

                     // Unregister the old listener before reloading
                     if (plugin.getBlockBreakHandler().getListener() != null) {
                        BlockBreakListener oldListener = plugin.getBlockBreakHandler().getListener();
                        BlockBreakEvent.getHandlerList().unregister(oldListener);
                     }

                     // Reload Config
                     plugin.reloadConfig();

                     // Process Config to reload Minable and Diggable entries
                     plugin.processConfig();

                     // Reinitialize BlockBreakHandler and Listener
                     plugin.getBlockBreakHandler().Init(plugin);
                     plugin.getServer().getPluginManager().registerEvents(plugin.getBlockBreakHandler().getListener(),
                           plugin);

                     // Reload available languages in the lang folder
                     plugin.loadAvailableLanguages();

                     // Ensure all old data is fully removed before re-registering
                     debuggingMessages.sendConsoleMessage("Registering new recipes after config reload...");
                     plugin.registerRecipeManager();

                     // Measure Time for the Reload
                     long endTime = System.currentTimeMillis(); // Get the end time
                     long duration = endTime - startTime; // Calculate the duration

                     // Send Message that the reload completed, including the time taken
                     sender.sendMessage(plugin.getLangFile().getMessage("reload-completed",
                           "&6Successfully reloaded the Plugin! Took %duration%ms", true)
                           .replace("%duration%", String.valueOf(duration)));
                     Bukkit.getConsoleSender().sendMessage(plugin.getLangFile().getMessage("reload-completed",
                           "&6Successfully reloaded the Plugin! Took %duration%ms", true)
                           .replace("%duration%", String.valueOf(duration)));
                     return true;
                  } else {
                     sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                           "&cYou do not have permission to use this command!", true));
                     return true;
                  }
               }
               // Command for "/powermining admin language"
               else if (args[1].equalsIgnoreCase("language")) {
                  if (sender.hasPermission("powermining.admin.language")) {
                     if (args.length == 3) {
                        String requestedLang = args[2];
                        if (plugin.doesLanguageExist(requestedLang)) {
                           // ensure config and language file exist
                           plugin.ensureConfigExists();
                           plugin.ensureLanguageFilesExist();

                           // Load Configuration from configFile and set the language
                           FileConfiguration Config = YamlConfiguration.loadConfiguration(plugin.ConfigFile);
                           Config.set("language", requestedLang);

                           // Save the updated configuration
                           try {
                              Config.save(plugin.ConfigFile);
                           } catch (IOException e) {
                              e.printStackTrace(); // Log an error if something goes wrong
                           }

                           // Reload configuration
                           plugin.reloadConfig();

                           // Reload available languages in the lang folder
                           plugin.loadAvailableLanguages();

                           // Reinitialize LangFile to reload language
                           String language = plugin.getConfig().getString("language", requestedLang);
                           plugin.setLangFile(new LangFile(plugin, language));

                           // Give atleast some feedback
                           sender.sendMessage(plugin.getLangFile().getMessage("language-set",
                                 "&6Successfully set the language file.", true));
                           return true;
                        } else {
                           // Language does not exist
                           sender.sendMessage(plugin.getLangFile().getMessage("language-not-exists",
                                 "&4The provided language does not exist in the \"lang\" folder of the plugin.", true));
                           return true;
                        }
                     } else {
                        sender.sendMessage(plugin.getLangFile().getMessage("language-usage",
                              "&4Usage: /<command> language <language>", true));
                        return true;
                     }
                  } else {
                     sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                           "&cYou do not have permission to use this command!", true));
                     return true;
                  }
               }
               // Command for "/powermining admin debug"
               else if (args[1].equalsIgnoreCase("debug")) {
                  if (sender.hasPermission("powermining.admin.debug")) {
                     // Toggle debug mode
                     boolean debug = plugin.getConfig().getBoolean("debug");
                     debug = !debug; // Toggle the value
                     plugin.getConfig().set("debug", debug);
                     plugin.saveConfig();

                     // Refresh DebuggingMessages instance to apply changes immediately
                     plugin.refreshDebuggingMessages();

                     // Send Message to player
                     String debugStatus = debug ? plugin.getLangFile().getMessage("enabled", "enabled", false)
                           : plugin.getLangFile().getMessage("disabled", "disabled", false);
                     String message = plugin.getLangFile().getMessage("debug-mode-set", "Debug mode is now: ", true)
                           + debugStatus;
                     sender.sendMessage(message);
                     return true;
                  } else {
                     sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                           "&cYou do not have permission to use this command!", true));
                     return true;
                  }
               }
               // /powermining admin help
               if (args[1].equalsIgnoreCase("help")) {
                  if (sender.hasPermission("powermining.admin")) {
                     for (String helpMessage : plugin.getLangFile().getMessageList("powermining-admin-help")) {
                        sender.sendMessage(helpMessage);
                     }
                     return true;
                  } else {
                     sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                           "&cYou do not have permission to use this command!", true));
                     return true;
                  }
               }
            } else {
               if (sender.hasPermission("powermining.admin")) {
                  for (String helpMessage : plugin.getLangFile().getMessageList("powermining-admin-help")) {
                     sender.sendMessage(helpMessage);
                  }
               } else {
                  sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                        "&cYou do not have permission to use this command!", true));
                  return true;
               }
            }
         }
         // Give Command
         else if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("g")) {
            if (sender instanceof Player) {
               if (p.hasPermission("powermining.give")) {
                  // Ensure args[1] exists and assign it to powerToolName
                  if (args.length < 2) {
                     sender.sendMessage(plugin.getLangFile().getMessage("missing-item-name",
                           "&cYou must specify an item name!", true));
                     return true;
                  }

                  powerToolName = args[1]; // Assign the item name from command arguments

                  final NamespacedKey namespacedKey = new NamespacedKey(plugin, powerToolName.toUpperCase());
                  final Recipe recipe = plugin.getServer().getRecipe(namespacedKey);

                  if (recipe == null) {
                     sender.sendMessage(plugin.getLangFile().getMessage("powertool-not-found",
                           "&cPowertool not found!", true));
                     return true;
                  }

                  if (p.getInventory().firstEmpty() == -1) {
                     sender.sendMessage(plugin.getLangFile().getMessage("inventory-full",
                           "&cYour inventory is full!", true));
                     return true;
                  }

                  p.getInventory().addItem(recipe.getResult());

                  sender.sendMessage(plugin.getLangFile().getMessage("power_tool_given",
                        "&2%tool% given to Player \"%player%\"", true)
                        .replace("%tool%", powerToolName)
                        .replace("%player%", sender.getName()));
                  return true;
               } else {
                  sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                        "&cYou do not have permission to use this command!", true));
                  return true;
               }
            } else {
               sender.sendMessage(plugin.getLangFile().getMessage("console-use-not-allowed",
                     "&cYou can't use that command from console.", true));
               return true;
            }
         }
         // Help Command
         else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")) {
            if (sender.hasPermission("powermining.use.commands")) {
               for (String helpMessage : plugin.getLangFile().getMessageList("powermining-help")) {
                  sender.sendMessage(helpMessage);
               }
               return true;
            } else {
               sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                     "&cYou do not have permission to use this command!", true));
               return true;
            }
         }
         // Info Command
         else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
            if (sender.hasPermission("powermining.use.commands")) {
               sender.sendMessage(plugin.getLangFile().getMessage("plugin-info",
                     "&2Plugin orginally made by &6bloodyshade&2, recreated by &6mgl23606&2, heavily modified and updated by &6Holt&2. See \"&4/powermining help&2\" for a list of subcommands.",
                     true));
               return true;
            } else {
               sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                     "&cYou do not have permission to use this command!", true));
               return true;
            }
         }
         // Version Command
         else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
            if (sender.hasPermission("powermining.version")) {
               sender.sendMessage(plugin.getLangFile().getMessage("version", "&2Version: %version%", true)
                     .replace("%version%", plugin.getDescription().getVersion()));
               return true;
            } else {
               sender.sendMessage(plugin.getLangFile().getMessage("no-command-permission",
                     "&cYou do not have permission to use this command!", true));
               return true;
            }
         }
         // Command not found
         else {
            sender.sendMessage(plugin.getLangFile().getMessage("powermining-cmd-not-found",
                  "&cThis command does not exist! Please use \"/powermining help\" for a list of subcommands.", true));
            return true;
         }
      }
      return true;
   }
}
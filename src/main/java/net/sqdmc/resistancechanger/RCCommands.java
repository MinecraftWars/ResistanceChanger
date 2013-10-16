package net.sqdmc.resistancechanger;

import java.util.HashMap;
import java.util.Set;
import java.util.Timer;

import net.sqdmc.resistancechanger.RCConfig;
import net.sqdmc.resistancechanger.ResistanceChanger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command class. Available commands are: rc rc reload rc info
 * 
 * @author Pandemoneus
 * 
 */
public final class RCCommands implements CommandExecutor {

    private ResistanceChanger plugin;

    /**
     * Associates this object with a plugin
     * 
     * @param plugin
     *            the plugin
     */
    public RCCommands(ResistanceChanger plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args != null) {
            if (sender instanceof Player) 
            {
                usePermissionsStructure((Player) sender, cmd, commandLabel, args);
            }
             else {
                sender.sendMessage(ChatColor.RED + "Sorry, you are not a player!");
            }
        }

        return true;
    }

    private void usePermissionsStructure(Player sender, Command cmd, String commandLabel, String[] args) {

        if (args.length == 0) {
            // show help
            if (sender.hasPermission("resistancechanger.help")) {
                showHelp(sender);
            } else {
                sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
            }
        } else if (args.length == 1) {
            // commands with 0 arguments
            String command = args[0];

            if (command.equalsIgnoreCase("reload")) {
                // reload
                if (sender.hasPermission("resistancechanger.config.reload")) {
                    reloadPlugin(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
                }
            } else if (command.equalsIgnoreCase("info")) {
                // info
                if (sender.hasPermission("resistancechanger.config.info")) {
                    getConfigInfo(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
                }
            } else if (command.equalsIgnoreCase("reset")) {
                // reset durabilities
                if (sender.hasPermission("resistancechanger.durability.reset")) {
                    resetDurability(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
                }
            } else if (command.equalsIgnoreCase("version")) {
                if (sender.hasPermission("resistancechanger.help")) {
                    sender.sendMessage(ChatColor.YELLOW + "ResistanceChanger version: " + ChatColor.GRAY + this.plugin.getDescription().getVersion());
                }
            }
        }
    }

    private void showHelp(Player sender) {
        sender.sendMessage(ChatColor.YELLOW + "-------------  " + ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Resistance Changer" + ChatColor.RESET + ChatColor.YELLOW + "  -------------");
        sender.sendMessage(ChatColor.YELLOW + "Available commands:");
        sender.sendMessage(ChatColor.AQUA + "/rc version - " + ChatColor.GOLD + " gives version and shows commands.");
        sender.sendMessage(ChatColor.AQUA + "/rc reload - " + ChatColor.GOLD + " reloads the plugin's config file");
        sender.sendMessage(ChatColor.AQUA + "/rc info - " + ChatColor.GOLD + " shows the currently loaded config");
        sender.sendMessage(ChatColor.AQUA + "/rc reset - " + ChatColor.GOLD + " reset all durability timers.");
    }

    private void reloadPlugin(Player sender) {
        ResistanceChanger.LOG.info("'" + sender.getName() + "' requested reload of resistancechanger");
        sender.sendMessage(ChatColor.GREEN + "Reloading ResistanceChanger!");

        if (plugin.reload()) {
            sender.sendMessage(ChatColor.GREEN + "Success!");
        }
    }

    private void getConfigInfo(Player sender) {
        sender.sendMessage(ChatColor.YELLOW + "Currently loaded config of ResistanceChanger:");
        sender.sendMessage(ChatColor.YELLOW + "---------------------------------------------");

        if (RCConfig.getInstance().getConfigList() != null) {
            for (String s : RCConfig.getInstance().getConfigList()) {
                sender.sendMessage(ChatColor.YELLOW + s);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "None - Config file deleted - please reload");
        }
    }

    private void resetDurability(Player sender) {
        /*BlockManager.getInstance().setObsidianDurability(new HashMap<Integer, Integer>());

        Set<Integer> set = BlockManager.getInstance().getObsidianTimer().keySet();

        for (Integer i : set) {
            BlockTimer t = BlockManager.getInstance().getObsidianTimer().get(i);

            if (t != null) {
                t.cancel();
            }
        }

        BlockManager.getInstance().setObsidianTimer(new HashMap<Integer, Timer>());*/

        ResistanceChanger.LOG.info("'" + sender.getName() + "' requested reset of all Block durabilities");
        sender.sendMessage(ChatColor.GREEN + "Reset all Block durabilities!");
    }
}

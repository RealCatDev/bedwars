package me.catdev.commands;

import me.catdev.Bedwars;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class BedwarsCommand implements CommandExecutor {

    private final Bedwars bedwars;

    public BedwarsCommand(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    private boolean handleMap(CommandSender sender, String label, String[] args) {
        String usage = "/"+label+" map";
        if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
            sender.sendMessage("Usage: " + usage + " <help|wizard>");
            return false;
        } else if (args[1].equalsIgnoreCase("wizard")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can execute this command!");
                return false;
            }
            if (args.length != 3) {
                sender.sendMessage("Usage: " + usage + " wizard <mapname>");
                return false;
            }
            if (args[2].equalsIgnoreCase("save")) {
                if (!this.bedwars.getMapManager().save((Player)sender)) {
                    sender.sendMessage("Failed to save!");
                }
            } else {
                this.bedwars.getMapManager().startWizard((Player) sender, args[2]);
            }
        }
        return true;
    }

    private boolean handleMatch(CommandSender sender, String label, String[] args) {
        String usage = "/"+label+" match <help|start>";
        if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
            sender.sendMessage(usage);
            return false;
        } else if (args[1].equalsIgnoreCase("start")) {
            this.bedwars.getMatchManager().StartCountdown(false);
        }
        return true;
    }

    private boolean handle(CommandSender sender, String label, String[] args) {
        String usage = "/"+label+" <map|match> <help>";

        if (args.length == 0) {
            sender.sendMessage("Usage: " + usage);
            return false;
        }

        if (args[0].equalsIgnoreCase("map")) {
            return handleMap(sender, label, args);
        } else if (args[0].equalsIgnoreCase("match")) {
            return handleMatch(sender, label, args);
        }

        sender.sendMessage("Usage: " + usage);
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof Player)) {
            Player plr = ((Player)sender);
            if (!plr.hasPermission("catdev.bedwars.manager")) return false;
        } else if (!(sender instanceof ConsoleCommandSender)) {
            return false;
        }
        return handle(sender, label, args);
    }

}
package me.catdev.commands;

import me.catdev.Bedwars;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class BedwarsCommand implements CommandExecutor {

    private final Bedwars bedwars;

    public BedwarsCommand(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    private final ArrayList<Player> overrideList = new ArrayList<>();

    private boolean handleMap(CommandSender sender, String label, String[] args) {
        String usage = "/"+label+" map";
        if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
            sender.sendMessage("Usage: " + usage + " <help|wizard|override>");
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
            } else if (args[2].equalsIgnoreCase("reset")) {
                if (!this.bedwars.getMapManager().isInWizard((Player) sender) || !this.bedwars.getMapManager().resetMap()) {
                    sender.sendMessage("You have to be in wizard to execute this command!");
                }
            } else if (args[2].equalsIgnoreCase("exit")) {
                if (!this.bedwars.getMapManager().isInWizard((Player) sender) || !this.bedwars.getMapManager().exitWizard((Player) sender)) {
                    sender.sendMessage("You have to be in wizard to execute this command!");
                }
            } else {
                this.bedwars.getMapManager().startWizard((Player) sender, args[2]);
            }
        } else if (args[1].equalsIgnoreCase("override")) {
            if (sender instanceof Player && !sender.hasPermission("catdev.bedwars.admin")) return false;
            else {
                if (!(sender instanceof Player) || overrideList.contains((Player)sender)) {
                    if (!this.bedwars.getMapManager().override()) {
                        sender.sendMessage("Failed to override!");
                    } else {
                        sender.sendMessage("Map has been successfully overridden!");
                    }
                } else {
                    sender.sendMessage("Are you sure you want to override the map? This action can't be undone.\nTo confirm type this command again.\nThis will automatically expire in 30s.");
                    overrideList.add((Player)sender);
                    new BukkitRunnable() {
                        @Override
                        public void run() {

                        }
                    }.runTaskLater(this.bedwars, 30*20);
                }
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
        String usage = "/"+label+" <map|match|reload> <help>";

        if (args.length == 0) {
            sender.sendMessage("Usage: " + usage);
            return false;
        }

        if (args[0].equalsIgnoreCase("map")) {
            return handleMap(sender, label, args);
        } else if (args[0].equalsIgnoreCase("match")) {
            return handleMatch(sender, label, args);
        } else if (args[0].equalsIgnoreCase("reload")) {
            this.bedwars.getMatchManager().Load();
            return true;
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
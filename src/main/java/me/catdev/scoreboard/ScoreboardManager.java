package me.catdev.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import me.catdev.Bedwars;
import me.catdev.common.ServerType;
import me.catdev.match.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ScoreboardManager {

    private final Bedwars bedwars;

    public ScoreboardManager(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    public void ShowScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        board.updateTitle(ChatColor.GOLD + "BEDWARS");
        if (this.bedwars.getSettingsManager().getServerType() == ServerType.MATCH) {
            if (this.bedwars.getMatchManager().isInProgress()) {
                ArrayList<Team> teams = this.bedwars.getMatchManager().getTeams();
                String[] lines = {
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "CatDevsBedwars"
                };
                lines[1] = "NEXT EVENT";
                int i = 0;
                for (; i < teams.size(); ++i) {
                    Team team = teams.get(i);
                    lines[3+i] = ChatColor.translateAlternateColorCodes('&', " "  + team.getTeamColor().getStyle() + team.getTeamColor().getName() + "&r: " + (team.isInUse()?(team.isBedAlive()?"✔":(team.isAlive()?(team.alivePlayerCount()):"x")):"x") + (team.containsPlayer(player)?" (you)":""));
                }
                int j = 3+i+1;
                lines[j] = "Kills: 0";
                board.updateLines(lines);
            }
        }
    }

}

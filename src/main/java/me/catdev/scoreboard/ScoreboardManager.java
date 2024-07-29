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
        if (this.bedwars.getSettings().serverType == ServerType.MATCH) {
            if (this.bedwars.getMatchManager().isInProgress()) {
                ArrayList<Team> teams = this.bedwars.getMatchManager().getTeams();
                ArrayList<String> lines = new ArrayList<>();
                lines.add("");
                lines.add("NEXT EVENT");
                lines.add("");
                for (Team team : teams) {
                    String line = " " + team.getTeamColor().getStyle() + team.getTeamColor().getName() + "&r: " + (team.isAlive()?(team.isBedAlive()?"✔":Integer.toString(team.alivePlayerCount())):"x") + (team.containsPlayer(player)?" (you)":"");
                    lines.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                lines.add("");
                lines.add("Kills: 0");
                board.updateLines(lines);
            }
        }
    }

}

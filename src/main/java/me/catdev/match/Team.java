package me.catdev.match;

import me.catdev.Bedwars;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Team {

    private final TeamColor teamColor;
    private final ArrayList<MatchPlayer> players = new ArrayList<>();
    private final ArrayList<Player> alivePlayers = new ArrayList<>();
    private final Location bedLoc;
    private boolean bedAlive;

    public Team(TeamColor teamColor, Location bedLoc) {
        this.teamColor = teamColor;
        this.bedLoc = bedLoc;
        this.bedAlive = true;
    }

    public Team(TeamColor teamColor) {
        this.teamColor = teamColor;
        this.bedLoc = null;
        this.bedAlive = false;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void addPlayer(MatchPlayer plr) {
        this.players.add(plr);
        this.alivePlayers.add(plr.getPlayer());
    }

    public void removePlayer(Player plr) {
        players.removeIf((MatchPlayer p) -> { return p.getPlayer().equals(plr); });
        alivePlayers.remove(plr);
    }

    public ArrayList<MatchPlayer> getPlayers() {
        return players;
    }

    // Returns true if bed is alive
    public boolean playerDied(Player plr) {
        if (!bedAlive) {
            alivePlayers.remove(plr);
        }
        return bedAlive;
    }

    public void destroyBed() {
        bedAlive = false;
    }

    public boolean isBedAlive() {
        return this.bedAlive;
    }

    public boolean isAlive() {
        return !alivePlayers.isEmpty();
    }

    public boolean isFull() {
        return players.size() == Bedwars.getInstance().getSettings().maxTeamCount;
    }

    public boolean containsPlayer(Player plr) {
        for (MatchPlayer matchPlr : players) {
            if (matchPlr.getPlayer() == plr) return true;
        }
        return false;
    }

    public int alivePlayerCount() {
        return alivePlayers.size();
    }

}

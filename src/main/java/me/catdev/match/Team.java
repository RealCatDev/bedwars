package me.catdev.match;

import me.catdev.Bedwars;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class Team implements ConfigurationSerializable {

    private final TeamColor teamColor;
    private Location genLoc;
    private Location bedLoc;
    private Location spawnLoc;

    private final ArrayList<MatchPlayer> players = new ArrayList<>();
    private final ArrayList<Player> alivePlayers = new ArrayList<>();
    private boolean bedAlive;

    public Team(TeamColor teamColor, Location genLoc, Location bedLoc, Location spawnLoc) {
        this.teamColor = teamColor;
        this.genLoc = genLoc;
        this.bedLoc = bedLoc;
        this.spawnLoc = spawnLoc;
        this.bedAlive = true;
    }

    public Team(TeamColor teamColor) {
        this.teamColor = teamColor;
        this.genLoc = null;
        this.bedLoc = null;
        this.spawnLoc = null;
        this.bedAlive = false;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public Location getGenerator() {
        return genLoc;
    }

    public Location getBedLoc() {
        return bedLoc;
    }

    public Location getSpawnLoc() {
        return spawnLoc;
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

    public void setGeneratorLocation(Location genLoc) {
        this.genLoc = genLoc;
    }

    public void setBedLoc(Location bedLoc) {
        this.bedLoc = bedLoc;
    }

    public void setSpawnLoc(Location spawnLoc) {
        this.spawnLoc = spawnLoc;
    }

    @Override
    public java.util.Map<String, Object> serialize() {
        java.util.Map<String, Object> serialized = new HashMap<>();
        serialized.put("team", teamColor.ordinal());
        serialized.put("generator", genLoc);
        serialized.put("bedLoc", bedLoc);
        serialized.put("spawnLoc", spawnLoc);
        return serialized;
    }

    public static Team deserialize(java.util.Map<String, Object> deserialize) {
        return new Team(
                TeamColor.values()[(int)deserialize.get("team")],
                (Location)deserialize.get("generator"),
                (Location)deserialize.get("bedLoc"),
                (Location)deserialize.get("spawnLoc"));
    }

}

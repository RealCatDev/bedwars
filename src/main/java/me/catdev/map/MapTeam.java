package me.catdev.map;

import me.catdev.match.TeamColor;
import me.catdev.match.generator.Generator;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;

public class MapTeam implements ConfigurationSerializable {

    // TODO: merge into Team

    private final TeamColor teamColor;
    private Location genLoc;
    private Location bedLoc;
    private Location spawnLoc;

    public MapTeam(TeamColor teamColor) {
        this.teamColor = teamColor;
        this.genLoc = null;
        this.bedLoc = null;
        this.spawnLoc = null;
    }

    public MapTeam(TeamColor teamColor, Location genLoc, Location bedLoc, Location spawnLoc) {
        this.teamColor = teamColor;
        this.genLoc = genLoc;
        this.bedLoc = bedLoc;
        this.spawnLoc = spawnLoc;
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

    public static MapTeam deserialize(java.util.Map<String, Object> deserialize) {
        return new MapTeam(
            TeamColor.values()[(int)deserialize.get("team")],
            (Location)deserialize.get("generator"),
            (Location)deserialize.get("bedLoc"),
            (Location)deserialize.get("spawnLoc"));
    }
}

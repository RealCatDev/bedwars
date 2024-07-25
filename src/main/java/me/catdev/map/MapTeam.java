package me.catdev.map;

import me.catdev.Bedwars;
import me.catdev.match.TeamColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MapTeam implements ConfigurationSerializable {

    private final TeamColor teamColor;
    private Location bedLoc;
    private ArrayList<Location> bounds;
    private Location spawnLoc;

    public MapTeam(TeamColor teamColor) {
        this.teamColor = teamColor;
        this.bedLoc = null;
        this.bounds = null;
        this.spawnLoc = null;
    }

    public MapTeam(TeamColor teamColor, Location bedLoc, ArrayList<Location> bounds, Location spawnLoc) {
        this.teamColor = teamColor;
        this.bedLoc = bedLoc;
        this.bounds = bounds;
        this.spawnLoc = spawnLoc;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public Location getBedLoc() {
        return bedLoc;
    }

    public ArrayList<Location> getBounds() {
        return bounds;
    }

    public Location getSpawnLoc() {
        return spawnLoc;
    }

    public void setBedLoc(Location bedLoc) {
        this.bedLoc = bedLoc;
    }

    public void setBounds(ArrayList<Location> bounds) {
        this.bounds = bounds;
    }

    public void setSpawnLoc(Location spawnLoc) {
        this.spawnLoc = spawnLoc;
    }

    @Override
    public java.util.Map<String, Object> serialize() {
        java.util.Map<String, Object> serialized = new HashMap<>();
        serialized.put("team", teamColor.ordinal());
        serialized.put("bedLoc", bedLoc);
        serialized.put("bounds", bounds);
        serialized.put("spawnLoc", spawnLoc);
        return serialized;
    }

    public static MapTeam deserialize(java.util.Map<String, Object> deserialize) {
        return new MapTeam(
            TeamColor.values()[NumberConversions.toInt(deserialize.get("team"))],
            Location.deserialize((java.util.Map<String, Object>) deserialize.get("bedLoc")),
            (ArrayList<Location>) ((List<Object>)deserialize.get("bounds")).stream().map(serializedLoc -> Location.deserialize((java.util.Map<String, Object>) serializedLoc)).collect(Collectors.toList()),
            Location.deserialize((java.util.Map<String, Object>) deserialize.get("spawnLoc"))
        );
    }
}

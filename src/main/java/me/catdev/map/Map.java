package me.catdev.map;

import me.catdev.Bedwars;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Map implements ConfigurationSerializable {

    private Location lobbySpawnLoc;
    private Location lobbyBound1;
    private Location lobbyBound2;
    private ArrayList<Location> bounds;
    private ArrayList<MapTeam> teams;
    private World world;

    public Map(Location lobbySpawnLoc, Location lobbyBound1, Location lobbyBound2, ArrayList<Location> bounds, ArrayList<MapTeam> teams) {
        this.lobbySpawnLoc = lobbySpawnLoc;
        this.lobbyBound1 = lobbyBound1;
        this.lobbyBound2 = lobbyBound2;
        this.bounds = bounds;
        this.teams = teams;
        this.world = null;
    }

    public boolean isComplete() {
        return lobbySpawnLoc != null && teams != null && world != null;
    }

    public Location getLobbySpawnLoc() {
        return lobbySpawnLoc;
    }

    public Location getLobbyBound1() {
        return lobbyBound1;
    }

    public Location getLobbyBound2() {
        return lobbyBound2;
    }

    public ArrayList<Location> getBounds() {
        return bounds;
    }

    public ArrayList<MapTeam> getTeams() {
        return teams;
    }

    public World getWorld() {
        return world;
    }

    public void setLobbySpawnLoc(Location lobbySpawnLoc) {
        this.lobbySpawnLoc = lobbySpawnLoc;
    }

    public void setLobbyBound1(Location lobbyBound1) {
        this.lobbyBound1 = lobbyBound1;
    }

    public void setLobbyBound2(Location lobbyBound2) {
        this.lobbyBound2 = lobbyBound2;
    }

    public void setBounds(ArrayList<Location> bounds) {
        this.bounds = bounds;
    }

    public void addBound(Location bound) {
        if (this.bounds == null) {
            this.bounds = new ArrayList<>();
        }
        this.bounds.add(bound);
    }

    public void setTeams(ArrayList<MapTeam> teams) {
        this.teams = teams;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public java.util.Map<String, Object> serialize() {
        if (bounds != null && (bounds.size()%2)==1) {
            bounds.remove(bounds.size()-1);
        }
        java.util.Map<String, Object> serialized = new HashMap<>();
        serialized.put("lobbySpawn", lobbySpawnLoc);
        serialized.put("lobbyBound1", lobbyBound1);
        serialized.put("lobbyBound2", lobbyBound2);
        serialized.put("bounds", bounds);
        serialized.put("teams", teams);
        return serialized;
    }

    public static Map deserialize(java.util.Map<String, Object> deserialize) {
        ArrayList<Location> bounds = null;
        if (deserialize.get("bounds") == null) bounds = new ArrayList<>();
        else {
            bounds = ((List<Location>) deserialize.get("bounds")).stream()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        ArrayList<MapTeam> teams = null;
        if (deserialize.get("teams") == null) teams = new ArrayList<>();
        else {
            teams = ((List<MapTeam>) deserialize.get("teams")).stream()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new Map(
                (Location)deserialize.get("lobbySpawn"),
                (Location)deserialize.get("lobbyBound1"),
                (Location)deserialize.get("lobbyBound2"),
                bounds,
                teams);
    }
}

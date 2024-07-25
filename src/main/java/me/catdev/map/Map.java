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

    private final String name;
    private Location lobbySpawnLoc;
    private Location lobbyBound1;
    private Location lobbyBound2;
    private ArrayList<MapTeam> teams;
    private final World world;

    public Map(String name) {
        this.name = name;
        this.lobbySpawnLoc = null;
        this.lobbyBound1 = null;
        this.lobbyBound2 = null;
        this.teams = null;
        this.world = Bedwars.getInstance().getServer().getWorld(name);
    }

    public Map(String name, Location lobbySpawnLoc, Location lobbyBound1, Location lobbyBound2, ArrayList<MapTeam> teams) {
        this.name = name;
        this.lobbySpawnLoc = lobbySpawnLoc;
        this.lobbyBound1 = lobbyBound1;
        this.lobbyBound2 = lobbyBound2;
        this.teams = teams;
        this.world = Bedwars.getInstance().getServer().getWorld(name);
    }

    public boolean isComplete() {
        return name != null && lobbySpawnLoc != null && lobbyBound1 != null && lobbyBound2 != null && teams != null && world != null;
    }

    public String getName() {
        return name;
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

    public void setTeams(ArrayList<MapTeam> teams) {
        this.teams = teams;
    }

    @Override
    public java.util.Map<String, Object> serialize() {
        java.util.Map<String, Object> serialized = new HashMap<>();
        serialized.put("name", name);
        serialized.put("lobbySpawn", lobbySpawnLoc);
        serialized.put("lobbyBound1", lobbyBound1);
        serialized.put("lobbyBound2", lobbyBound2);
        serialized.put("teams", teams);
        return serialized;
    }

    public static Map deserialize(java.util.Map<String, Object> deserialize) {
        ArrayList<MapTeam> teams = null;
        if (deserialize.get("teams") == null) teams = new ArrayList<>();
        else {
            teams = ((List<Object>) deserialize.get("teams")).stream()
                    .map(serializedTeam -> MapTeam.deserialize((java.util.Map<String, Object>) serializedTeam))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new Map((String) deserialize.get("name"),
                (Location)deserialize.get("lobbySpawn"),
                (Location)deserialize.get("lobbyBound1"),
                (Location)deserialize.get("lobbyBound2"),
                teams);
    }
}

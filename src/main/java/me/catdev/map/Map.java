package me.catdev.map;

import me.catdev.match.generator.GenLoot;
import me.catdev.match.generator.Generator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

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
    private ArrayList<GenLoot> teamLoot;
    private ArrayList<Location> diamondGenerators;
    private ArrayList<Location> emeraldGenerators;
    private World world;

    public Map(Location lobbySpawnLoc, Location lobbyBound1, Location lobbyBound2, ArrayList<Location> bounds, ArrayList<MapTeam> teams, ArrayList<GenLoot> teamLoot, ArrayList<Location> diamondGenerators, ArrayList<Location> emeraldGenerators) {
        this.lobbySpawnLoc = lobbySpawnLoc;
        this.lobbyBound1 = lobbyBound1;
        this.lobbyBound2 = lobbyBound2;
        this.bounds = bounds;
        this.teams = teams;
        this.teamLoot = teamLoot;
        this.diamondGenerators = diamondGenerators;
        this.emeraldGenerators = emeraldGenerators;
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

    public ArrayList<GenLoot> getTeamLoot() {
        return teamLoot;
    }

    public ArrayList<Location> getDiamondGenerators() {
        return diamondGenerators;
    }

    public ArrayList<Location> getEmeraldGenerators() {
        return emeraldGenerators;
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

    public void setTeamLoot(ArrayList<GenLoot> teamLoot) {
        this.teamLoot = teamLoot;
    }

    public void setDiamondGenerators(ArrayList<Location> diamondGenerators) {
        this.diamondGenerators = diamondGenerators;
    }

    public void addDiamondGenerator(Location gen) {
        this.diamondGenerators.add(gen);
    }

    public void setEmeraldGenerators(ArrayList<Location> emeraldGenerators) {
        this.emeraldGenerators = emeraldGenerators;
    }

    public void addEmeraldGenerator(Location gen) {
        this.emeraldGenerators.add(gen);
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
        serialized.put("islandLoot", teamLoot);
        serialized.put("diamondGenerators", diamondGenerators);
        serialized.put("emeraldGenerators", emeraldGenerators);
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
        ArrayList<GenLoot> teamLoot = null;
        if (deserialize.get("islandLoot") == null) {
            teamLoot = new ArrayList<>();
            teamLoot.add(new GenLoot(Material.IRON_INGOT, 1, 1));
            teamLoot.add(new GenLoot(Material.GOLD_INGOT, 1, 10));
        } else {
            teamLoot = ((List<GenLoot>) deserialize.get("islandLoot")).stream()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        ArrayList<Location> diamondGenerators = null;
        if (deserialize.get("diamondGenerators") == null) diamondGenerators = new ArrayList<>();
        else {
            diamondGenerators = ((List<Location>) deserialize.get("diamondGenerators")).stream()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        ArrayList<Location> emeraldGenerators = null;
        if (deserialize.get("emeraldGenerators") == null) emeraldGenerators = new ArrayList<>();
        else {
            emeraldGenerators = ((List<Location>) deserialize.get("emeraldGenerators")).stream()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new Map(
                (Location)deserialize.get("lobbySpawn"),
                (Location)deserialize.get("lobbyBound1"),
                (Location)deserialize.get("lobbyBound2"),
                bounds,
                teams,
                teamLoot,
                diamondGenerators,
                emeraldGenerators);
    }
}

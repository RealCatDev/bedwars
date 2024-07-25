package me.catdev.match;

import org.bukkit.entity.Player;

public class MatchPlayer {

    private final Player player;
    private final Team team;
    private boolean isAlive;

    public MatchPlayer(Player player, Team team) {
        this.player = player;
        this.team = team;
        this.isAlive = true;
    }

    public Player getPlayer() {
        return player;
    }

    public Team getTeam() {
        return team;
    }

    public boolean isAlive() {
        return isAlive;
    }
}

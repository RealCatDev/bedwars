package me.catdev.match;

import org.bukkit.entity.Player;

public class MatchPlayer {

    private final Player player;
    private TeamColor team;

    public MatchPlayer(Player player, TeamColor team) {
        this.player = player;
        this.team = team;
    }

    public Player getPlayer() {
        return player;
    }

    public TeamColor getTeam() {
        return team;
    }

    public void setTeam(TeamColor team) {
        this.team = team;
    }
}

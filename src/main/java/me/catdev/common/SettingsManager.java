package me.catdev.common;

import me.catdev.Bedwars;
import me.catdev.config.ConfigManager;

import java.io.IOException;

public class SettingsManager {

    private final Bedwars bedwars;

    public SettingsManager(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    private ServerType serverType;
    private boolean logJoin = false;
    private boolean logLeave = false;

    // Match settings
    private int maxTeamSize = 1;
    private int maxTeamCount = 8;
    private int maxPlayerCount = 8;
    private int minPlayerCount = 2;

    public ServerType getServerType() {
        return serverType;
    }

    public boolean doLogJoin() {
        return logJoin;
    }

    public boolean doLogLeave() {
        return logLeave;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public int getMaxTeamCount() {
        return maxTeamCount;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public int getMinPlayerCount() {
        return minPlayerCount;
    }

    public boolean load() {
        try {
            serverType = ServerType.valueOf(((String) this.bedwars.getConfig().get("settings.type", "LOBBY")).toUpperCase());
        } catch (IllegalArgumentException ex) {
            serverType = ServerType.LOBBY;
        }
        logJoin = (boolean)this.bedwars.getConfig().get("settings.logJoin", false);
        logLeave = (boolean)this.bedwars.getConfig().get("settings.logLeave", false);
        // Match exclusive:
        maxTeamSize = (int) this.bedwars.getConfig().get("settings.maxTeamSize", 1);
        maxTeamCount = (int) this.bedwars.getConfig().get("settings.maxTeamCount", 8);
        maxPlayerCount = (int) this.bedwars.getConfig().get("settings.maxPlayerCount", maxTeamSize*maxTeamCount);
        minPlayerCount = (int) this.bedwars.getConfig().get("settings.minPlayerCount", maxPlayerCount/4);

        return true;
    }

}

package me.catdev.common;

import me.catdev.Bedwars;
import me.catdev.config.ConfigManager;

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
        ConfigManager config = this.bedwars.getConfigManager();
        String serverTypeStr = config.getString("settings.type");
        if (serverTypeStr.equalsIgnoreCase("lobby")) {
            serverType = ServerType.LOBBY;
        } else if (serverTypeStr.equalsIgnoreCase("match")) {
            serverType = ServerType.MATCH;
        } else {
            return false;
        }

        Object logJoinObj = config.getObject("settings.logJoin");
        if (logJoinObj != null && (boolean)logJoinObj) {
            logJoin = true;
        }

        Object logLeaveObj = config.getObject("settings.logLeave");
        if (logLeaveObj != null && (boolean)logLeaveObj) {
            logLeave = true;
        }

        Object maxTeamPlayerCountObj = config.getObject("settings.maxTeamSize");
        if (maxTeamPlayerCountObj instanceof Integer) {
            maxTeamSize = (int)maxTeamPlayerCountObj;
        }

        Object maxTeamCountObj = config.getObject("settings.maxTeamCount");
        if (maxTeamCountObj instanceof Integer) {
            maxTeamCount = (int)maxTeamCountObj;
        }

        Object maxPlayerCountObj = config.getObject("settings.maxPlayerCount");
        if (maxPlayerCountObj instanceof Integer) {
            maxPlayerCount = (int)maxPlayerCountObj;
        } else {
            maxPlayerCount = maxTeamSize *maxPlayerCount;
        }

        Object minPlayerCountObj = config.getObject("settings.minPlayerCount");
        if (minPlayerCountObj instanceof Integer) {
            minPlayerCount = (int)minPlayerCountObj;
        } else {
            minPlayerCount = maxPlayerCount/4;
        }

        return true;
    }

}

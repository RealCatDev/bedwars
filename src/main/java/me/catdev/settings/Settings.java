package me.catdev.settings;

import me.catdev.common.ServerType;

public class Settings {

    public Settings(ServerType serverType, boolean logJoin, boolean logLeave, int maxTeamSize, int maxTeamCount, int maxPlayerCount, int minPlayerCount) {
        this.serverType = serverType;
        this.logJoin = logJoin;
        this.logLeave = logLeave;
        this.maxTeamSize = maxTeamSize;
        this.maxTeamCount = maxTeamCount;
        this.maxPlayerCount = maxPlayerCount;
        this.minPlayerCount = minPlayerCount;
    }

    public ServerType serverType;
    public boolean logJoin = false;
    public boolean logLeave = false;
    public int maxTeamSize = 1;
    public int maxTeamCount = 8;
    public int maxPlayerCount = 8;
    public int minPlayerCount = 2;

    private static Object getOrDefault(java.util.Map<String, Object> deserialize, String path, Object def) {
        if (!deserialize.containsKey(path)) return def;
        return deserialize.get(path);
    }

    public static Settings deserialize(java.util.Map<String, Object> deserialize) {
        String serverTypeStr = (String) deserialize.get("type");
        ServerType serverType = null;
        try {
            serverType = ServerType.valueOf(((serverTypeStr!=null)?serverTypeStr:"lobby").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        if (serverType == null) serverType = ServerType.LOBBY;
        if (serverType == ServerType.MATCH) {
            int maxTeamSize = (int) getOrDefault(deserialize, "maxTeamSize", 1);
            int maxTeamCount = (int) getOrDefault(deserialize, "maxTeamCount", 8);
            int maxPlayerCount = (int) getOrDefault(deserialize, "maxPlayerCount", maxTeamSize*maxTeamCount);
            int minPlayerCount = (int) getOrDefault(deserialize, "minPlayerCount", maxPlayerCount/4);
            return new Settings(
                    serverType, (boolean) getOrDefault(deserialize, "logJoin", true), (boolean) getOrDefault(deserialize, "logLeave", true),
                    maxTeamSize, maxTeamCount, maxPlayerCount, minPlayerCount
            );
        } else {
            return null;
        }
    }

}

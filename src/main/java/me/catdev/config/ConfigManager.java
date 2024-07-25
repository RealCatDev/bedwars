package me.catdev.config;

import me.catdev.Bedwars;

public class ConfigManager {

    private final Bedwars bedwars;

    public ConfigManager(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    public String getString(String path) {
        if (!this.bedwars.getConfig().contains(path)) return null;
        return (String)this.bedwars.getConfig().get(path);
    }

    public Object getObject(String path) {
        if (!this.bedwars.getConfig().contains(path)) return null;
        return this.bedwars.getConfig().get(path);
    }

}

package me.catdev.settings;

import me.catdev.Bedwars;
import me.catdev.common.ServerType;
import me.catdev.utils.ConfigUtils;

import java.util.Map;

public class SettingsManager {

    public static void load(Bedwars bedwars) {
        bedwars.setSettings(Settings.deserialize((Map<String, Object>) ConfigUtils.convertToMap(bedwars.getConfig()).get("settings")));
    }

}

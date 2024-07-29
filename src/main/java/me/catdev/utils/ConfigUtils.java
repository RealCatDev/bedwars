package me.catdev.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class ConfigUtils {

    public static java.util.Map<String, Object> convertToMap(ConfigurationSection section) {
        java.util.Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                value = convertToMap((ConfigurationSection) value);
            }
            map.put(key, value);
        }
        return map;
    }

}

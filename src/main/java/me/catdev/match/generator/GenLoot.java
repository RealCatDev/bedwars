package me.catdev.match.generator;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class GenLoot implements ConfigurationSerializable {

    private final Material mat;
    private final int count;
    private final int rarity;

    public GenLoot(Material mat, int count, int rarity) {
        this.mat = mat;
        this.count = count;
        this.rarity = rarity;
    }

    public Material getMaterial() {
        return mat;
    }

    public int getCount() {
        return count;
    }

    public int getRarity() {
        return rarity;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialize = new HashMap<>();
        serialize.put("material", mat.name());
        serialize.put("count", count);
        serialize.put("rarity", rarity);
        return serialize;
    }

    public static GenLoot deserialize(java.util.Map<String, Object> deserialize) {
        return new GenLoot(
                Material.valueOf((String)deserialize.get("material")),
                (int)deserialize.get("count"),
                (int)deserialize.get("rarity"));
    }
}

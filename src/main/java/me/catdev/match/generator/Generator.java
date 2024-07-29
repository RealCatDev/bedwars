package me.catdev.match.generator;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generator extends BukkitRunnable implements ConfigurationSerializable {

    private final Location loc;
    private List<GenLoot> loot;
    private int tickCounter = 0;

    public Generator(Location loc, List<GenLoot> loot) {
        this.loc = loc;
        this.loot = loot;
    }

    public void setLoot(List<GenLoot> loot) {
        this.loot = loot;
    }

    @Override
    public void run() {
        boolean reset = true;

        for (GenLoot loot : this.loot) {
            int rarity = loot.getRarity();

            if ((rarity-1) > tickCounter) reset = false;
            else {
                Item i = this.loc.getWorld().dropItem(this.loc, new ItemStack(loot.getMaterial(), loot.getCount()));
                i.setVelocity(new Vector(0.0f, 0.0f, 0.0f));
                i.setPickupDelay(0);
            }
        }

        if (reset) {
            tickCounter = 0;
        } else {
            tickCounter++;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialize = new HashMap<>();
        serialize.put("location", loc);
        serialize.put("loot", loot);
        return serialize;
    }

    public static Generator deserialize(java.util.Map<String, Object> deserialize) {
        return new Generator(
                (Location)deserialize.get("location"),
                (List<GenLoot>)deserialize.get("loot"));
    }
}

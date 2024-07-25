package me.catdev.events;

import me.catdev.Bedwars;
import me.catdev.utils.InventoryHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryEvents implements Listener {

    private final Bedwars bedwars;

    public InventoryEvents(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        if (ev.getInventory() == null || !(ev.getInventory().getHolder() instanceof Player)) return;
        if (!bedwars.isInvOpen((Player)ev.getInventory().getHolder())) return;

        InventoryHelper inv = bedwars.getInventory((Player)ev.getInventory().getHolder());
        if (inv == null) return;
        inv.onInvClick(ev);
    }

}

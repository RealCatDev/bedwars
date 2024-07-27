package me.catdev.utils;

import me.catdev.Bedwars;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryHelper {

    protected final Bedwars bedwars;
    protected final Player player;
    protected final int size;
    private final Inventory inventory;

    public InventoryHelper(Bedwars bedwars, Player player, int size) { // size - Size of my (CatDev's) cock (big)
        this.bedwars = bedwars;
        this.player = player;
        this.size = size;
        this.inventory = this.bedwars.getServer().createInventory(player, this.size);
        construct();
    }

    public InventoryHelper(Bedwars bedwars, Player player, int size, String title) {
        this.bedwars = bedwars;
        this.player = player;
        this.size = size;
        this.inventory = this.bedwars.getServer().createInventory(player, this.size, title);
        construct();
    }

    protected void setItem(int slot, ItemStack item) {
        this.inventory.setItem(slot, item);
    }

    protected void setItem(int slot, Material material, int amount) {
        setItem(slot, new ItemStack(material, amount));
    }

    protected abstract void construct();

    public void showInventory() {
        this.player.openInventory(this.inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract void onInvClick(InventoryClickEvent ev);
}
package me.catdev;

import me.catdev.commands.BedwarsCommand;
import me.catdev.common.ServerType;
import me.catdev.common.SettingsManager;
import me.catdev.config.ConfigManager;
import me.catdev.events.InventoryEvents;
import me.catdev.map.Map;
import me.catdev.map.MapManager;
import me.catdev.map.MapTeam;
import me.catdev.match.MatchManager;
import me.catdev.scoreboard.ScoreboardManager;
import me.catdev.utils.InventoryHelper;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class Bedwars extends JavaPlugin {

    private final ConfigManager configManager = new ConfigManager(this);
    private final SettingsManager settingsManager = new SettingsManager(this);
    private final MatchManager matchManager = new MatchManager(this);
    private final MapManager mapManager = new MapManager(this);
    private final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
    static private Bedwars instance = null;
    private final HashMap<Player, InventoryHelper> inventories = new HashMap<>();

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public void openInventory(InventoryHelper inv) {
        inv.showInventory();
        inventories.put(inv.getPlayer(), inv);
    }

    public void closeInventory(InventoryHelper inv) {
        inventories.remove(inv);
    }

    public InventoryHelper getInventory(Player prl) {
        if (!isInvOpen(prl))
            return null;
        return inventories.get(prl);
    }

    public boolean isInvOpen(Player plr) {
        return inventories.containsKey(plr);
    }

    public static Bedwars getInstance() {
        return instance;
    }

    static {
        ConfigurationSerialization.registerClass(Map.class);
        ConfigurationSerialization.registerClass(MapTeam.class);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        if (!this.settingsManager.load()) {
            this.getLogger().warning("Failed to enable me.catdev.bedwars! (settingsManager.load())");
            return;
        }
        this.getLogger().info("me.catdev.bedwars enabled!");
        this.getCommand("bedwars").setExecutor(new BedwarsCommand(this));
        this.getServer().getPluginManager().registerEvents(new InventoryEvents(this), this);
        if (this.settingsManager.getServerType() == ServerType.MATCH) {
            this.getServer().getPluginManager().registerEvents(this.matchManager, this);
            this.getServer().getPluginManager().registerEvents(this.mapManager, this);
            this.mapManager.Init();
            this.mapManager.Load();
            if (!this.matchManager.Load()) return;
        }
    }

    @Override
    public void onDisable() {
        this.getLogger().info("me.catdev.bedwars disabled!");
    }
}

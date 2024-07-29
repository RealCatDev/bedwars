package me.catdev;

import me.catdev.commands.BedwarsCommand;
import me.catdev.common.ServerType;
import me.catdev.match.generator.GenLoot;
import me.catdev.match.generator.Generator;
import me.catdev.settings.Settings;
import me.catdev.settings.SettingsManager;
import me.catdev.config.ConfigManager;
import me.catdev.events.EnvEvents;
import me.catdev.events.InventoryEvents;
import me.catdev.map.Map;
import me.catdev.map.MapManager;
import me.catdev.map.MapTeam;
import me.catdev.match.MatchManager;
import me.catdev.scoreboard.ScoreboardManager;
import me.catdev.utils.InventoryHelper;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class Bedwars extends JavaPlugin {

    private final ConfigManager configManager = new ConfigManager(this);
    private final MatchManager matchManager = new MatchManager(this);
    private final MapManager mapManager = new MapManager(this);
    private final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
    static private Bedwars instance = null;
    private final HashMap<Player, InventoryHelper> inventories = new HashMap<>();

    private Settings settings = null;

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public ConfigManager getConfigManager() {
        return configManager;
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

    public Settings getSettings() {
        return settings;
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
        ConfigurationSerialization.registerClass(Generator.class);
        ConfigurationSerialization.registerClass(GenLoot.class);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        SettingsManager.load(this);

        this.getLogger().info("==========[ CatDevsBedwars ]==========");
        this.getLogger().info("Config:");
        this.getLogger().info("  logJoin:        "+this.settings.logJoin);
        this.getLogger().info("  logLeave:       "+this.settings.logLeave);
        this.getLogger().info("  type:           "+this.settings.serverType.name().toLowerCase());
        if (this.settings.serverType == ServerType.LOBBY) {

        } else if (this.settings.serverType == ServerType.MATCH) {
            this.getLogger().info("  maxTeamSize:    "+this.settings.maxTeamSize);
            this.getLogger().info("  maxTeamCount:   "+this.settings.maxTeamCount);
            this.getLogger().info("  maxPlayerCount: "+this.settings.maxPlayerCount);
            this.getLogger().info("  minPlayerCount: "+this.settings.minPlayerCount);
        }
        this.getLogger().info("==========[ CatDevsBedwars ]==========");

        this.getLogger().info("me.catdev.bedwars enabled!");
        this.getCommand("bedwars").setExecutor(new BedwarsCommand(this));
        this.getServer().getPluginManager().registerEvents(new InventoryEvents(this), this);
        this.getServer().getPluginManager().registerEvents(new EnvEvents(), this);
        if (this.settings.serverType == ServerType.MATCH) {
            this.getServer().getPluginManager().registerEvents(this.matchManager, this);
            this.getServer().getPluginManager().registerEvents(this.mapManager, this);
            this.mapManager.Init();
            if (!this.matchManager.Load()) {
                this.getLogger().severe("Shit went bad");
            }
        }
    }

    @Override
    public void onDisable() {
        if (!this.mapManager.unloadMap("default")) {
            this.getLogger().severe("Failed to unload map!");
        }
    }
}

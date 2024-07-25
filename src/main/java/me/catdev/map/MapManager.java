package me.catdev.map;

import me.catdev.Bedwars;
import me.catdev.config.ConfigManager;
import me.catdev.match.TeamColor;
import me.catdev.utils.InventoryHelper;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class MapManager implements Listener {

    private final Bedwars bedwars;
    private Player playerInWizard = null;
    private Map map = null;

    public MapManager(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void Load() {
        ConfigManager config = this.bedwars.getConfigManager();
        this.name = config.getString("map.name");
        if (this.name == null) {
            this.name = "world";
        }
    }

    public void UnloadMap(String mapname) {
        Bukkit.unloadWorld(mapname, false);
    }

    private Location getLocation(FileConfiguration config, World world, String path) {
        return new Location(world, config.getDouble(path+".x"), config.getDouble(path+".y"), config.getDouble(path+".z"), (float)config.getDouble(path+".yaw"), (float)config.getDouble(path+".pitch"));
    }

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

    private Map LoadMap(String mapname, World world) {
        File mapFile = new File(this.bedwars.getDataFolder(), mapname+".yml");
        if (mapFile.exists()) {
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(mapFile);
            } catch (IOException | InvalidConfigurationException ex) {
                ex.printStackTrace();
                return null;
            }
            java.util.Map<String, Object> meh = convertToMap(config);
            return (Map)meh.get("map");
        } else {
            return new Map(mapname);
        }
    }

    public Map LoadMap(String mapname) {
        World w = new WorldCreator(mapname).createWorld();
        w.setAutoSave(false);
        this.map = LoadMap(mapname, w);
        return this.map;
    }

    private final ItemStack setLobbyItem = new ItemStack(Material.STICK, 1);
    private final ItemStack lobbyPos1Item = new ItemStack(Material.STICK, 1);
    private final ItemStack lobbyPos2Item = new ItemStack(Material.STICK, 1);
    private final ItemStack teamSelectorItem = new ItemStack(Material.WOOL, 1, (short)14);
    private final ItemStack setSpawnItem = new ItemStack(Material.STICK, 1);
    private final ItemStack setBedItem = new ItemStack(Material.BED, 1);
    private TeamColor wizardSelectedTeam = null;
    private ArrayList<MapTeam> teams = null;

    public void Init() {
        {
            ItemMeta meta = setLobbyItem.getItemMeta();
            meta.setDisplayName("Set lobby");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click when selected to set current location as lobby spawn");
            meta.setLore(lore);
            setLobbyItem.setItemMeta(meta);
        }
        {
            ItemMeta meta = lobbyPos1Item.getItemMeta();
            meta.setDisplayName("Set lobby pos1");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click on block to set it's location as lobby pos1");
            meta.setLore(lore);
            lobbyPos1Item.setItemMeta(meta);
        }
        {
            ItemMeta meta = lobbyPos2Item.getItemMeta();
            meta.setDisplayName("Set lobby pos2");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click on block to set it's location as lobby pos2");
            meta.setLore(lore);
            lobbyPos2Item.setItemMeta(meta);
        }
        {
            ItemMeta meta = teamSelectorItem.getItemMeta();
            meta.setDisplayName("Team selector");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click to open team selector menu");
            meta.setLore(lore);
            teamSelectorItem.setItemMeta(meta);
        }
        {
            ItemMeta meta = setSpawnItem.getItemMeta();
            meta.setDisplayName("Set spawn");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click to set your current position as selected team's spawn!");
            meta.setLore(lore);
            setSpawnItem.setItemMeta(meta);
        }
        {
            ItemMeta meta = setBedItem.getItemMeta();
            meta.setDisplayName("Set bed");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click on bed to select it as selected team's bed!");
            meta.setLore(lore);
            setBedItem.setItemMeta(meta);
        }
    }

    public boolean startWizard(Player plr, String mapname) {
        if (playerInWizard != null) return false;
        playerInWizard = plr;
        UnloadMap(mapname);
        Map m = LoadMap(mapname);
        plr.teleport(new Location(m.getWorld(), 0, 0, 0));
        wizardSelectTeam(null);
        plr.setGameMode(GameMode.CREATIVE);
        return true;
    }

    public boolean saveMap() {
        File mapFile = new File(this.bedwars.getDataFolder(), map.getName()+".yml");
        if (!mapFile.exists()) {
            try {
                mapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        FileConfiguration config = new YamlConfiguration();
        config.set("map", map);
        try {
            config.save(mapFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean save(Player plr) {
        map.setTeams(teams);
        if (!playerInWizard.getUniqueId().equals(plr.getUniqueId()))
            return false;
        return saveMap();
    }

    public boolean exitWizard(Player plr) {
        if (playerInWizard != plr) return false;
        playerInWizard = null;
        return true;
    }

    public boolean isInWizard(Player plr) {
        return playerInWizard == plr;
    }

    public void wizardSelectTeam(TeamColor team) {
        this.wizardSelectedTeam = team;
        Inventory inv = this.playerInWizard.getInventory();
        inv.clear();
        if (team == null) {
            inv.setItem(0, setLobbyItem);
            inv.setItem(1, lobbyPos1Item);
            inv.setItem(2, lobbyPos2Item);
        } else {
            teamSelectorItem.setData(new MaterialData(Material.WOOL, (byte)team.getData()));
            inv.setItem(0, setSpawnItem);
            inv.setItem(1, setBedItem);
//            inv.setItem(0, setBedItem);
//            inv.setItem(0, setBedItem);
        }
        inv.setItem(8, teamSelectorItem);
    }

    static class TeamSelectorInv extends InventoryHelper {
        public TeamSelectorInv(Bedwars bedwars, Player player) {
            super(bedwars, player, 9*3);
        }

        @Override
        protected void construct() {
            ItemStack empty = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
            {
                ItemMeta meta = empty.getItemMeta();
                meta.setDisplayName(" ");
                empty.setItemMeta(meta);
            }
            for (int i = 0; i < 9*3; ++i) {
                ItemStack is = null;
                if (i < 9 || i >= 18) is = empty;
                if (i >= 9 && i < 18) {
                    if ((i-9) < this.bedwars.getSettingsManager().getMaxTeamCount()) {
                        is = new ItemStack(Material.WOOL, 1, TeamColor.values()[i-9].getData());
                        ItemMeta newMeta = is.getItemMeta();
                        newMeta.setDisplayName(TeamColor.values()[i-9].getName());
                        is.setItemMeta(newMeta);
                    } else is = empty;
                }
                setItem(i, is);
            }
        }

        @Override
        public void onInvClick(InventoryClickEvent ev) {
            ev.setCancelled(true);
            int slot = ev.getSlot();
            if (slot < 9 || slot >= 18) return;
            this.bedwars.getMapManager().wizardSelectTeam(TeamColor.values()[slot-9]);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (playerInWizard == null) return;
        Player plr = ev.getPlayer();
        if (playerInWizard.getUniqueId() != plr.getUniqueId()) return;
        ItemStack itemStack = ev.getItem();
        if (itemStack == null) return;
        ev.setCancelled(true);
        if (itemStack.getItemMeta().equals(setLobbyItem.getItemMeta())) {
            map.setLobbySpawnLoc(plr.getLocation());
        } else if (itemStack.getItemMeta().equals(lobbyPos1Item.getItemMeta())) {
            if (ev.getClickedBlock() == null) {
                ev.setCancelled(false);
                return;
            }
            map.setLobbyBound1(ev.getClickedBlock().getLocation());
        } else if (itemStack.getItemMeta().equals(lobbyPos2Item.getItemMeta())) {
            if (ev.getClickedBlock() == null) {
                ev.setCancelled(false);
                return;
            }
            map.setLobbyBound2(ev.getClickedBlock().getLocation());
        } else if (itemStack.getItemMeta().equals(teamSelectorItem.getItemMeta())) {
            TeamSelectorInv inv = new TeamSelectorInv(this.bedwars, plr);
            this.bedwars.openInventory(inv);
        } else if (itemStack.getItemMeta().equals(setSpawnItem.getItemMeta())) {
            map.setLobbySpawnLoc(plr.getLocation());
        } else {
            ev.setCancelled(false);
        }
    }

}

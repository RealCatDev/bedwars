package me.catdev.map;

import me.catdev.Bedwars;
import me.catdev.config.ConfigManager;
import me.catdev.match.Team;
import me.catdev.match.TeamColor;
import me.catdev.match.generator.GenLoot;
import me.catdev.match.generator.Generator;
import me.catdev.utils.ConfigUtils;
import me.catdev.utils.FileUtils;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapManager implements Listener {

    private final Bedwars bedwars;
    private Player playerInWizard = null;
    private final HashMap<String, World> mapWorlds = new HashMap<>();
    private final HashMap<String, File> mapFiles = new HashMap<>();

    public MapManager(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    public boolean unloadMap(String mapname) {
        if (!mapWorlds.containsKey(mapname)) return false;
        Bukkit.unloadWorld(mapWorlds.get(mapname), false);
        FileUtils.delete(mapFiles.get(mapname));
        mapWorlds.remove(mapname);
        mapFiles.remove(mapname);
        return true;
    }

    private Location getLocation(FileConfiguration config, World world, String path) {
        return new Location(world, config.getDouble(path+".x"), config.getDouble(path+".y"), config.getDouble(path+".z"), (float)config.getDouble(path+".yaw"), (float)config.getDouble(path+".pitch"));
    }

    private String GetWorldPath(String mapname) {
        File listFile = new File(this.bedwars.getDataFolder(), "mapList.yml");
        if (listFile.exists()) {
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(listFile);
            } catch (IOException | InvalidConfigurationException ex) {
                ex.printStackTrace();
                return null;
            }
            return (String)((java.util.Map<String, Object>)ConfigUtils.convertToMap(config).get("maps")).get(mapname);
        }
        return null;
    }

    private Map LoadMap(String mapname) {
        File mapsFile = new File(this.bedwars.getDataFolder(), "maps.yml");
        if (mapsFile.exists()) {
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(mapsFile);
            } catch (IOException | InvalidConfigurationException ex) {
                ex.printStackTrace();
                return null;
            }
            return (Map)ConfigUtils.convertToMap(config).get(mapname);
        }
        return null;
    }

    private World CopyWorld(String mapname, File src) {
        File dst = new File(Bukkit.getWorldContainer().getParent(), mapname);
        try {
            FileUtils.copy(src, dst);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        World w = new WorldCreator(mapname).createWorld();
        if (w != null) {
            w.setAutoSave(false);
            w.setGameRuleValue("doDaylightCycle", "false");
        }
        if (mapFiles.containsKey(mapname)) {
            mapFiles.remove(mapname);
            mapWorlds.remove(mapname);
        }
        mapFiles.put(mapname, dst);
        mapWorlds.put(mapname, w);
        return w;
    }

    public Map loadMap(String mapname) {
        String worldPath = GetWorldPath(mapname);
        if (worldPath == null) return null;
        World w = CopyWorld(mapname, new File(this.bedwars.getDataFolder(), worldPath));
        Map map = LoadMap(mapname);
        if (map != null) {
            map.setWorld(w);
        } else {
            unloadMap(mapname);
        }
        return map;
    }

    private final ItemStack setLobbyItem = new ItemStack(Material.STICK, 1);
    private final ItemStack lobbyPos1Item = new ItemStack(Material.STICK, 1);
    private final ItemStack lobbyPos2Item = new ItemStack(Material.STICK, 1);
    private final ItemStack boundItem = new ItemStack(Material.BARRIER, 1);
    private final ItemStack teamSelectorItem = new ItemStack(Material.WOOL, 1, (short)14);
    private final ItemStack setSpawnItem = new ItemStack(Material.STICK, 1);
    private final ItemStack setBedItem = new ItemStack(Material.BED, 1);
    private final ItemStack setGeneratorItem = new ItemStack(Material.IRON_INGOT, 1);
    private final ItemStack diamondGenItem = new ItemStack(Material.DIAMOND, 1);
    private final ItemStack emeraldGenItem = new ItemStack(Material.EMERALD, 1);
    private TeamColor wizardSelectedTeam = null;
    private ArrayList<Team> teams = null;
    private List<GenLoot> defaultIslandLoot = null;
    private String wizardMapname = null;
    private String wizardWorldPath = null;
    private Map wizardMap = null;

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
            lore.add("Right click on a block to set it's location as lobby pos1");
            meta.setLore(lore);
            lobbyPos1Item.setItemMeta(meta);
        }
        {
            ItemMeta meta = lobbyPos2Item.getItemMeta();
            meta.setDisplayName("Set lobby pos2");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click on a block to set it's location as lobby pos2");
            meta.setLore(lore);
            lobbyPos2Item.setItemMeta(meta);
        }
        {
            ItemMeta meta = boundItem.getItemMeta();
            meta.setDisplayName("Add binding");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click on a block to add it's location to bindings");
            lore.add("If it was a mistake you can save before adding second bound to remove it if it wasn't complete");
            meta.setLore(lore);
            boundItem.setItemMeta(meta);
        }
        {
            ItemMeta meta = teamSelectorItem.getItemMeta();
            meta.setDisplayName("Team selector");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click to open the team selector menu");
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
            lore.add("Right click on a bed to select it as selected team's bed!");
            meta.setLore(lore);
            setBedItem.setItemMeta(meta);
        }
        {
            ItemMeta meta = setGeneratorItem.getItemMeta();
            meta.setDisplayName("Set generator");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click to set your current position as selected team's generator!");
            meta.setLore(lore);
            setGeneratorItem.setItemMeta(meta);
        }
        {
            ItemMeta meta = diamondGenItem.getItemMeta();
            meta.setDisplayName("Add diamond generator");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click to add new diamond generator with your current position!");
            meta.setLore(lore);
            diamondGenItem.setItemMeta(meta);
        }
        {
            ItemMeta meta = emeraldGenItem.getItemMeta();
            meta.setDisplayName("Add diamond generator");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Right click to add new emerald generator with your current position!");
            meta.setLore(lore);
            emeraldGenItem.setItemMeta(meta);
        }
    }

    public boolean startWizard(Player plr, String mapname) {
        if (playerInWizard != null) return false;
        playerInWizard = plr;
        unloadMap(mapname);
        wizardMapname = mapname;
        wizardMap = loadMap(mapname);
        teams = wizardMap.getTeams();
        defaultIslandLoot = new ArrayList<>();
        defaultIslandLoot.add(new GenLoot(Material.IRON_INGOT, 1, 1));
        defaultIslandLoot.add(new GenLoot(Material.GOLD_INGOT, 1, 10));
        wizardWorldPath = GetWorldPath(mapname);
        plr.teleport(new Location(wizardMap.getWorld(), 0, 0, 0));
        wizardSelectTeam(null);
        plr.setGameMode(GameMode.CREATIVE);
        return true;
    }

    public boolean override() {
        if (wizardMap == null) return false;
        Bukkit.unloadWorld(wizardMap.getWorld(), true);
        try {
            FileUtils.copy(
                    new File(Bukkit.getWorldContainer().getParent(), wizardMapname),
                    new File(this.bedwars.getDataFolder(), wizardWorldPath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean saveMap(String mapname, Map map) {
        File mapFile = new File(this.bedwars.getDataFolder(), "maps.yml");
        if (!mapFile.exists()) {
            try {
                mapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        FileConfiguration config = new YamlConfiguration();
        config.set(mapname, map);
        try {
            config.save(mapFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean resetMap() {
        if (this.wizardMap == null) return false;
        this.wizardMap = new Map(null, null, null, null, null, null, null, null);
        return true;
    }

    public boolean save(Player plr) {
        if (playerInWizard == null || !playerInWizard.getUniqueId().equals(plr.getUniqueId())) {
            plr.sendMessage("You are not in map wizard!");
            return false;
        }
        wizardMap.setTeams(teams);
        boolean b = saveMap(wizardMapname, wizardMap);
        if (b) {
            plr.sendMessage("Map saved successfully!");
        }
        return b;
    }

    public boolean exitWizard(Player plr) {
        if (playerInWizard == null || playerInWizard.getUniqueId().equals(plr.getUniqueId())) return false;
        playerInWizard = null;
        unloadMap(wizardMapname);
        wizardMapname = null;
        wizardWorldPath = null;
        wizardMap = null;
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
            inv.setItem(3, boundItem);
            inv.setItem(4, diamondGenItem);
            inv.setItem(5, emeraldGenItem);
        } else {
            teamSelectorItem.setData(new MaterialData(Material.WOOL, (byte)team.getData()));
            inv.setItem(0, setSpawnItem);
            inv.setItem(1, setBedItem);
            inv.setItem(2, setGeneratorItem);
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
                    if ((i-9) < this.bedwars.getSettings().maxTeamCount) {
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

    private void assertTeamAvailable(int index) {
        if (this.teams == null) {
            this.teams = new ArrayList<>();
        }
        if (this.teams.size() < index+1) {
            for (int i = this.teams.size(); i < index+1; ++i) {
                this.teams.add(new Team(TeamColor.values()[i]));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev) {
        Player plr = ev.getPlayer();
        ItemStack itemStack = ev.getItem();
        if (playerInWizard == null || playerInWizard.getUniqueId() != plr.getUniqueId() || itemStack == null) return;
        ev.setCancelled(true);
        if (itemStack.getItemMeta().equals(setLobbyItem.getItemMeta())) {
            wizardMap.setLobbySpawnLoc(plr.getLocation());
        } else if (itemStack.getItemMeta().equals(lobbyPos1Item.getItemMeta())) {
            if (ev.getClickedBlock() == null) {
                return;
            }
            wizardMap.setLobbyBound1(ev.getClickedBlock().getLocation());
        } else if (itemStack.getItemMeta().equals(lobbyPos2Item.getItemMeta())) {
            if (ev.getClickedBlock() == null) {
                return;
            }
            wizardMap.setLobbyBound2(ev.getClickedBlock().getLocation());
        } else if (itemStack.getItemMeta().equals(boundItem.getItemMeta())) {
            if (ev.getClickedBlock() == null) {
                return;
            }
            wizardMap.addBound(ev.getClickedBlock().getLocation());
        } else if (itemStack.getItemMeta().equals(teamSelectorItem.getItemMeta())) {
            TeamSelectorInv inv = new TeamSelectorInv(this.bedwars, plr);
            this.bedwars.openInventory(inv);
        } else if (itemStack.getItemMeta().equals(setSpawnItem.getItemMeta())) {
            assertTeamAvailable(this.wizardSelectedTeam.ordinal());
            this.teams.get(this.wizardSelectedTeam.ordinal()).setSpawnLoc(plr.getLocation());
        } else if (itemStack.getItemMeta().equals(setBedItem.getItemMeta())) {
            assertTeamAvailable(this.wizardSelectedTeam.ordinal());
            this.teams.get(this.wizardSelectedTeam.ordinal()).setBedLoc(ev.getClickedBlock().getLocation());
        } else if (itemStack.getItemMeta().equals(setGeneratorItem.getItemMeta())) {
            assertTeamAvailable(this.wizardSelectedTeam.ordinal());
            this.teams.get(this.wizardSelectedTeam.ordinal()).setGeneratorLocation(plr.getLocation());
        } else if (itemStack.getItemMeta().equals(diamondGenItem.getItemMeta())) {
            if (this.wizardMap.getDiamondGenerators() == null) this.wizardMap.setDiamondGenerators(new ArrayList<>());
            this.wizardMap.addDiamondGenerator(plr.getLocation());
        } else if (itemStack.getItemMeta().equals(emeraldGenItem.getItemMeta())) {
            if (this.wizardMap.getEmeraldGenerators() == null) this.wizardMap.setEmeraldGenerators(new ArrayList<>());
            this.wizardMap.addEmeraldGenerator(plr.getLocation());
        } else {
            ev.setCancelled(false);
        }
    }

}

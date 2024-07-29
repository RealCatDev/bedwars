package me.catdev.match;

import me.catdev.Bedwars;
import me.catdev.map.Map;
import me.catdev.match.generator.GenLoot;
import me.catdev.match.generator.Generator;
import me.catdev.utils.Evaluator;
import me.catdev.utils.InventoryHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Bed;
import org.bukkit.material.Directional;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MatchManager implements Listener {

    static class Countdown extends BukkitRunnable {
        private final Bedwars bedwars;
        private int count = 0;
        private final Player plr;
        private final boolean startOnFinish;

        public Countdown(Bedwars bedwars, Player plr, int count) {
            this.bedwars = bedwars;
            this.plr = plr;
            this.count = count;
            this.startOnFinish = false;
        }

        public Countdown(Bedwars bedwars, int count) {
            this.bedwars = bedwars;
            this.plr = null;
            this.count = count;
            this.startOnFinish = true;
        }

        enum ServerPackage {

            MINECRAFT("net.minecraft.server." + getServerVersion());

            private final String path;

            ServerPackage(String path) {
                this.path = path;
            }

            public static String getServerVersion() {
                return Bukkit.getServer().getClass().getPackage().getName().substring(23);
            }

            @Override
            public String toString() {
                return path;
            }

            public Class<?> getClass(String className) throws ClassNotFoundException {
                return Class.forName(this.toString() + "." + className);
            }

        }

        private void ShowTitle(Player plr) {
            try {
                Object entityPlayer = plr.getClass().getMethod("getHandle").invoke(plr);
                Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                // NMS Classes
                Class<?> clsPacketPlayOutTitle = ServerPackage.MINECRAFT.getClass("PacketPlayOutTitle");
                Class<?> clsPacket = ServerPackage.MINECRAFT.getClass("Packet");
                Class<?> clsIChatBaseComponent = ServerPackage.MINECRAFT.getClass("IChatBaseComponent");
                Class<?> clsChatSerializer = ServerPackage.MINECRAFT.getClass("IChatBaseComponent$ChatSerializer");
                Class<?> clsEnumTitleAction = ServerPackage.MINECRAFT.getClass("PacketPlayOutTitle$EnumTitleAction");
                Object timesPacket = clsPacketPlayOutTitle.getConstructor(int.class, int.class, int.class).newInstance(2, 15, 3);
                playerConnection.getClass().getMethod("sendPacket", clsPacket).invoke(playerConnection, timesPacket);
                Object titleComponent = clsChatSerializer.getMethod("a", String.class).invoke(null, Integer.toString(count));
                Object titlePacket = clsPacketPlayOutTitle.getConstructor(clsEnumTitleAction, clsIChatBaseComponent).newInstance(clsEnumTitleAction.getField("TITLE").get(null), titleComponent);
                playerConnection.getClass().getMethod("sendPacket", clsPacket).invoke(playerConnection, titlePacket);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            plr.playSound(plr.getLocation(), Sound.NOTE_PLING, 10.0f, 1.0f);
        }

        @Override
        public void run() {
            if (count == 0) {
                this.bedwars.getServer().getScheduler().cancelTask(this.getTaskId());
                if (startOnFinish) this.bedwars.getMatchManager().Start();
            } else if (count < 0) {
                return;
            } else if (count <= 5 || count == 10 || (count % 15) == 0) {
                if (this.plr == null) {
                    for (Player plr : this.bedwars.getServer().getOnlinePlayers()) {
                        ShowTitle(plr);
                    }
                } else {
                    ShowTitle(plr);
                }
            }
            --count;
        }
    }

    static class Finish extends BukkitRunnable {

        private final Bedwars bedwars;

        public Finish(Bedwars bedwars) {
            this.bedwars = bedwars;
        }

        @Override
        public void run() {
            this.bedwars.getMatchManager().Load();
        }
    }

    static class Respawn extends BukkitRunnable {
        private final Player player;
        private final Location respawnLoc;

        public Respawn(Player player, Location respawnLoc) {
            this.player = player;
            this.respawnLoc = respawnLoc;
        }

        @Override
        public void run() {
            player.setGameMode(GameMode.SURVIVAL);
            player.setFallDistance(0);
            player.teleport(respawnLoc);
            player.setFallDistance(0);
        }
    }

    static class TeamSelectorInv extends InventoryHelper {
        private static int calcSize() {
            int maxTeamCount = Bedwars.getInstance().getSettings().maxTeamCount;
            int teamRows = (int) Math.ceil(maxTeamCount/4.0);
            return (teamRows*9)+9*(3+teamRows-1);
        }

        public TeamSelectorInv(Bedwars bedwars, Player player) {
            super(bedwars, player, calcSize(), "Team selector");
        }

        @Override
        protected void construct() {
            ItemStack empty = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
            {
                ItemMeta meta = empty.getItemMeta();
                meta.setDisplayName(" ");
                empty.setItemMeta(meta);
            }
            int i = 0;
            for (int teamIdx = 0; i < this.size-9; ++i) {
                int row = i/9;
                if ((row % 2) == 0) {
                    setItem(i, empty);
                } else {
                    int col = i%9;
                    if ((col%2)==0) {
                        setItem(i, empty);
                    } else {
                        TeamColor team = TeamColor.values()[teamIdx++];
                        ItemStack teamItem = new ItemStack(Material.WOOL, 1, team.getData());
                        ItemMeta meta = teamItem.getItemMeta();
                        meta.setDisplayName(team.getName());
                        teamItem.setItemMeta(meta);
                        setItem(i, teamItem);
                    }
                }
            }
            for (int j = 0; i < this.size; ++i, ++j) {
                if (j < 8) setItem(i, empty);
                else if (j == 8) {
                    ItemStack is = new ItemStack(Material.BARRIER, 1);
                    ItemMeta meta = is.getItemMeta();
                    meta.setDisplayName("Cancel team selection");
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add("Click to cancel team reservation");
                    meta.setLore(lore);
                    is.setItemMeta(meta);
                    setItem(i, is);
                }
            }
        }

        @Override
        public void onInvClick(InventoryClickEvent ev) {
            ev.setCancelled(true);

            ItemStack item = ev.getCurrentItem();
            if (item.getType() == Material.WOOL) {
                TeamColor team = null;
                for (TeamColor teamColor : TeamColor.values()) {
                    if (teamColor.getData() == item.getData().getData()) team = teamColor;
                }
                if (team == null) return;
                if (!this.bedwars.getMatchManager().selectTeam(this.player, team)) {
                    this.player.sendMessage("Team is full!");
                    return;
                }

                {
                    ItemStack selectorItem = item.clone();
                    ItemMeta meta = selectorItem.getItemMeta();
                    meta.setDisplayName("Team selector");
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add("Press right click when selected to open team selection menu");
                    meta.setLore(lore);
                    selectorItem.setItemMeta(meta);
                    this.player.getInventory().setItem(0, selectorItem);
                }
            } else if (item.getType() == Material.BARRIER) {
                {
                    ItemStack selectorItem = new ItemStack(Material.WOOL, 1, TeamColor.WHITE.getData());
                    ItemMeta meta = selectorItem.getItemMeta();
                    meta.setDisplayName("Team selector");
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add("Press right click when selected to open team selection menu");
                    meta.setLore(lore);
                    selectorItem.setItemMeta(meta);
                    this.player.getInventory().setItem(0, selectorItem);
                }
                this.bedwars.getMatchManager().selectTeam(this.player, null);
            }
            ev.getWhoClicked().closeInventory();
        }
    }

    private MatchState matchState = MatchState.LOADING;
    private Countdown countdown = null;
    private Map map = null;
    private final PotionEffect saturation = new PotionEffect(PotionEffectType.SATURATION, 99999, 1, false, false);
    private ArrayList<Player> players = null;
    private ArrayList<Player> lobbyPlayers = null;
    private ArrayList<Generator> teamGenerators = null;
    private ArrayList<Generator> diamondGenerators = null;
    private ArrayList<Generator> emeraldGenerators = null;
    private HashMap<Player, MatchPlayer> matchPlayers = null;

    private final Bedwars bedwars;

    public MatchManager(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    private void showScoreboard() {
        for (Player prl : players) {
            this.bedwars.getScoreboardManager().ShowScoreboard(prl);
        }
    }

    private void safeTeleport(Player plr, Location loc) {
        plr.setFallDistance(0);
        plr.teleport(loc);
        plr.setFallDistance(0);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        Player plr = ev.getPlayer();
        if (matchState == MatchState.LOADING ||
            matchState == MatchState.INGAME ||
            matchState == MatchState.FINISH) {
            ev.setJoinMessage("");
            plr.kickPlayer("Match is in progress/loading!");
            return;
        }
        if (players.size() >= this.bedwars.getSettings().maxPlayerCount) {
            ev.setJoinMessage("");
            plr.kickPlayer("Match is full!");
            return;
        }
        players.add(plr);
        lobbyPlayers.add(plr);

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("playerName", plr.getDisplayName());
        variables.put("playerCount", players.size());
        variables.put("maxPlayerCount", this.bedwars.getSettings().maxPlayerCount);

        String message = this.bedwars.getConfigManager().getString("locales.en.joinMessage");
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = Evaluator.evaluate(message, variables);
        ev.setJoinMessage(message);

        if (matchState == MatchState.LOBBY && players.size() >= this.bedwars.getSettings().minPlayerCount) {
            StartCountdown(true);
        }

        safeTeleport(plr, map.getLobbySpawnLoc());
        plr.getInventory().clear();
        {
            ItemStack selectorItem = new ItemStack(Material.WOOL, 1, TeamColor.WHITE.getData());
            ItemMeta meta = selectorItem.getItemMeta();
            meta.setDisplayName("Team selector");
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Press right click when selected to open team selection menu");
            meta.setLore(lore);
            selectorItem.setItemMeta(meta);
            plr.getInventory().setItem(0, selectorItem);
        }
        plr.setGameMode(GameMode.SURVIVAL);
        saturation.apply(plr);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent ev) {
        Player plr = ev.getPlayer();
        if (!players.contains(plr)) return;
        players.remove(plr);
        if (lobbyPlayers != null) lobbyPlayers.remove(plr);

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("playerName", plr.getDisplayName());
        variables.put("playerCount", players.size());
        variables.put("maxPlayerCount", this.bedwars.getSettings().maxPlayerCount);

        String message = this.bedwars.getConfigManager().getString("locales.en.leaveMessage");
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = Evaluator.evaluate(message, variables);
        ev.setQuitMessage(message);

        if (matchState == MatchState.STARTING && players.size() < this.bedwars.getSettings().minPlayerCount) {
            matchState = MatchState.LOBBY;
            countdown.cancel();
            countdown = null;
        }

        if (matchState == MatchState.INGAME) {
            // TODO: Players should be able to rejoin
            this.map.getTeams().get(matchPlayers.get(plr).getTeam().ordinal()).removePlayer(plr);
            matchPlayers.remove(plr);
            showScoreboard();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent ev) {
        ev.getDrops().clear();
        Player plr = ev.getEntity();
        plr.setHealth(plr.getMaxHealth());
        switch (matchState) {
            case LOBBY:
            case STARTING: {
                safeTeleport(plr, map.getLobbySpawnLoc());
                plr.setGameMode(GameMode.SURVIVAL);
            } break;
            case INGAME: {
                MatchPlayer matchPlayer = matchPlayers.get(plr);
                Location newLoc = map.getLobbySpawnLoc();
                newLoc.setY(newLoc.getY()+6);
                plr.setGameMode(GameMode.SPECTATOR);
                safeTeleport(plr, newLoc);
                boolean b = this.map.getTeams().get(matchPlayer.getTeam().ordinal()).playerDied(plr);
                int aliveTeams = 0;
                for (Team team : this.map.getTeams()) {
                    if (team.isAlive()) {
                        ++aliveTeams;
                    }
                }
                if (b && aliveTeams > 1) {
                    Location respawnLoc = this.map.getTeams().get(matchPlayers.get(plr).getTeam().ordinal()).getSpawnLoc();
                    if (respawnLoc == null) respawnLoc = new Location(map.getWorld(), 0.5, 1, 0.5);
                    new Respawn(plr, respawnLoc).runTaskLater(this.bedwars, 20*5L);
                    new Countdown(this.bedwars, plr, 5).runTaskTimer(this.bedwars, 0L, 20L);
                } else {
                    ev.setDeathMessage(ev.getDeathMessage() + " Final kill!");
                    Finish();
                }
            } break;
            case FINISH: {
                safeTeleport(plr, new Location(map.getWorld(), 0.5, 1, 0.5));
                plr.setGameMode(GameMode.SURVIVAL);
                plr.setAllowFlight(true);
            }
            default: {
            }
        }
        showScoreboard();
        saturation.apply(plr);
    }

    private boolean boundsCheck(Location loc0, Location loc1, Location loc2) {
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        int x0 = loc0.getBlockX();
        int y0 = loc0.getBlockY();
        int z0 = loc0.getBlockZ();

        return (x0 >= minX && x0 <= maxX) &&
                (y0 >= minY && y0 <= maxY) &&
                (z0 >= minZ && z0 <= maxZ);
    }

    private boolean isBedFromLocation(Bed bed, Location bedLoc, Location loc) {
        if (bed.isHeadOfBed()) {
            switch (bed.getFacing()) {
            case NORTH: {
                loc.add(0, 0, 1);
            } break;
            case SOUTH: {
                loc.subtract(0, 0, 1);
            } break;
            case EAST: {
                loc.add(1, 0, 0);
            } break;
            case WEST: {
                loc.subtract(1, 0, 0);
            } break;
            default: return false;
            }
        }
        return bedLoc.equals(loc);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent ev) {
        if (ev.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (matchState == MatchState.INGAME) {
                for (int i = 0; i < this.map.getBounds().size(); i+=2) {
                    if (boundsCheck(ev.getBlock().getLocation(), this.map.getBounds().get(i), this.map.getBounds().get(i+1))) {
                        ev.setCancelled(true);
                        break;
                    }
                }
            } else {
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent ev) {
        if (ev.getBlock().getType().equals(Material.BED_BLOCK)) {
            ev.setCancelled(true);
            if (matchState == MatchState.INGAME) {
                boolean found = false;
                for (Team team : this.map.getTeams()) {
                    int idx = team.getTeamColor().ordinal();
                    if (this.map.getTeams().size() <= idx) continue;
                    Location bedLoc = this.map.getTeams().get(idx).getBedLoc();
                    if (bedLoc == null || !this.map.getTeams().get(idx).isAlive()) continue;
                    Block bedBlock = ev.getBlock();
                    if (!(bedBlock.getState().getData() instanceof Directional) || !(bedBlock.getState().getData() instanceof Bed) || !isBedFromLocation((Bed)bedBlock.getState().getData(), bedLoc, bedBlock.getLocation())) continue;
                    if (this.matchPlayers.get(ev.getPlayer()).getTeam().ordinal() == idx) {
                        ev.getPlayer().sendMessage("You can't break your own bed!");
                        return;
                    }
                    team.destroyBed();
                    ev.getBlock().setType(Material.AIR);
                    showScoreboard();
                    found = true;
                    break;
                }
                if (!found) ev.getPlayer().sendMessage("This bed is not used by any team!");
            }
        } else if (ev.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (matchState == MatchState.INGAME) {
                for (int i = 0; i < this.map.getBounds().size(); i+=2) {
                    if (boundsCheck(ev.getBlock().getLocation(), this.map.getBounds().get(i), this.map.getBounds().get(i+1))) {
                        ev.setCancelled(true);
                        break;
                    }
                }
            } else {
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (isInProgress()) return;
        ev.setCancelled(true);
        ItemStack item = ev.getItem();
        if (item == null || item.getItemMeta() == null) return;
        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("Team selector") && (ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            TeamSelectorInv inv = new TeamSelectorInv(this.bedwars, ev.getPlayer());
            this.bedwars.openInventory(inv);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent ev) {
        if (!isInProgress() && ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);
    }

    public boolean Load() {
        matchState = MatchState.LOADING;
        players = new ArrayList<>();
        lobbyPlayers = new ArrayList<>();
        matchPlayers = new HashMap<>();
        for (Player plr : this.bedwars.getServer().getOnlinePlayers()) {
            plr.kickPlayer("Reloading lol");
        }
        if (this.map != null) {
            if (!this.bedwars.getMapManager().unloadMap("default")) {
                this.bedwars.getLogger().severe("FAILED TO UNLOAD MAP!");
            }
        }
        map = this.bedwars.getMapManager().loadMap("default");
        if (map == null) return false;
        if (!map.isComplete()) {
            map.setLobbySpawnLoc(new Location(map.getWorld(), 0.5, 12.0, 0.5, 0.0f, 0.0f));
            map.setLobbyBound1(new Location(map.getWorld(), 4.0, 11.0, 4.0, 0.0f, 0.0f));
            map.setLobbySpawnLoc(new Location(map.getWorld(), -4.0, 15.0, -4.0, 0.0f, 0.0f));
            map.setTeams(new ArrayList<>());
        }
        map.getWorld().setGameRuleValue("doDaylightCycle", "false");
        teamGenerators = null;
        diamondGenerators = null;
        emeraldGenerators = null;
        matchState = MatchState.LOBBY;

        for (int i = this.map.getTeams().size(); i < this.bedwars.getSettings().maxTeamCount; ++i) {
            this.map.addTeam(new Team(TeamColor.values()[i], null, null, new Location(map.getWorld(), 0, 1, 0)));
        }

        return true;
    }

    public void StartCountdown(boolean b) {
        matchState = MatchState.STARTING;
        ShowCountdown(b?30:5);
    }

    public void Start() {
        Stack<Player> playerStack = new Stack<>();
        ListIterator<Player> it = lobbyPlayers.listIterator(lobbyPlayers.size());
        while (it.hasPrevious()) {
            playerStack.push(it.previous());
        }
        teamGenerators = new ArrayList<>();
        for (int i = 0; i < this.bedwars.getSettings().maxTeamCount; ++i) {
            Team team = this.map.getTeams().get(i);
            while (!team.isFull() && !playerStack.isEmpty()) {
                Player prl = playerStack.pop();
                MatchPlayer matchPrl = new MatchPlayer(prl, TeamColor.values()[i]);
                matchPlayers.put(prl, matchPrl);
                team.addPlayer(matchPrl);
            }
            for (MatchPlayer matchPlr : team.getPlayers()) {
                Player plr = matchPlr.getPlayer();
                Location respawnLoc = this.map.getTeams().get(team.getTeamColor().ordinal()).getSpawnLoc();
                if (respawnLoc == null) respawnLoc = new Location(map.getWorld(), 0.5, 1, 0.5);
                safeTeleport(plr, respawnLoc);
                plr.getInventory().clear();
                plr.setHealth(plr.getMaxHealth());
            }
            {
                Location loc = this.map.getTeams().get(i).getGenerator();
                if (loc == null) continue;
                Generator gen = new Generator(loc, this.map.getTeamLoot());
                gen.runTaskTimer(this.bedwars, 0L, 20L);
                teamGenerators.add(gen);
            }
        }
        lobbyPlayers = null;

        List<GenLoot> diamondLoot = new ArrayList<>();
        diamondLoot.add(new GenLoot(Material.DIAMOND, 1, 30));
        List<GenLoot> emeraldLoot = new ArrayList<>();
        emeraldLoot.add(new GenLoot(Material.EMERALD, 1, 60));

        diamondGenerators = new ArrayList<>();
        for (Location loc : map.getDiamondGenerators()) {
            Generator gen = new Generator(loc, diamondLoot);
            gen.runTaskTimer(this.bedwars, 0L, 20L);
            diamondGenerators.add(gen);
        }

        emeraldGenerators = new ArrayList<>();
        for (Location loc : map.getEmeraldGenerators()) {
            Generator gen = new Generator(loc, emeraldLoot);
            gen.runTaskTimer(this.bedwars, 0L, 20L);
            emeraldGenerators.add(gen);
        }

        if (map.getLobbyBound1() != null && map.getLobbyBound2() != null) { // Fill lobby with air
            int sx = Math.min(map.getLobbyBound1().getBlockX(), map.getLobbyBound2().getBlockX());
            int sy = Math.min(map.getLobbyBound1().getBlockY(), map.getLobbyBound2().getBlockY());
            int sz = Math.min(map.getLobbyBound1().getBlockZ(), map.getLobbyBound2().getBlockZ());
            int dx = Math.max(map.getLobbyBound1().getBlockX(), map.getLobbyBound2().getBlockX());
            int dy = Math.max(map.getLobbyBound1().getBlockY(), map.getLobbyBound2().getBlockY());
            int dz = Math.max(map.getLobbyBound1().getBlockZ(), map.getLobbyBound2().getBlockZ());
            for (int x = sx; x <= dx; ++x) {
                for (int y = sy; y <= dy; ++y) {
                    for (int z = sz; z <= dz; ++z) {
                        this.map.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }

        matchState = MatchState.INGAME;
        showScoreboard();
    }

    public boolean selectTeam(Player plr, TeamColor teamColor) {
        if (isInProgress()) return false;
        MatchPlayer matchPlr = null;
        if (matchPlayers.containsKey(plr)) {
            matchPlr = matchPlayers.get(plr);
            this.map.getTeams().get(matchPlr.getTeam().ordinal()).removePlayer(plr);
            if (teamColor == null) {
                matchPlayers.remove(plr);
            } else {
                matchPlr.setTeam(teamColor);
            }
        }
        Team newTeam = ((teamColor==null)?null:this.map.getTeams().get(teamColor.ordinal()));
        if (newTeam != null && newTeam.isFull()) return false;
        if (newTeam != null) {
            if (matchPlr == null) {
                matchPlr = new MatchPlayer(plr, teamColor);
                matchPlayers.put(plr, matchPlr);
            }
            lobbyPlayers.remove(plr);
            newTeam.addPlayer(matchPlr);
        } else if (!lobbyPlayers.contains(plr)) {
            lobbyPlayers.add(plr);
        }
        return true;
    }

    void Finish() {
        matchState = MatchState.FINISH;
        TeamColor winningTeam = matchPlayers.get(players.get(0)).getTeam();
        for (Team team : this.map.getTeams()) {
            if (team.isAlive()) {
                winningTeam = team.getTeamColor();
                break;
            }
        }
        for (Generator gen : teamGenerators)    gen.cancel();
        for (Generator gen : diamondGenerators) gen.cancel();
        for (Generator gen : emeraldGenerators) gen.cancel();
        for (Player prl : players) {
            if (matchPlayers.get(prl).getTeam() == winningTeam) {
                prl.setAllowFlight(true);
            }
            prl.sendMessage(winningTeam.getName() + " won!");
        }
        new Finish(this.bedwars).runTaskLater(this.bedwars, 20*5); // Kick players after 5 seconds.
    }

    void ShowCountdown(int count) {
        if (countdown != null) {
            countdown.cancel();
        }
        countdown = new Countdown(this.bedwars, count);
        countdown.runTaskTimer(this.bedwars, 0L, 20L);
    }

    public boolean isInProgress() {
        return matchState == MatchState.INGAME || matchState == MatchState.FINISH;
    }

    public ArrayList<Team> getTeams() {
        return this.map.getTeams();
    }
}

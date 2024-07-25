package me.catdev.match;

import me.catdev.Bedwars;
import me.catdev.common.ServerType;
import me.catdev.map.Map;
import me.catdev.utils.Evaluator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class MatchManager implements Listener {

    static class Countdown extends BukkitRunnable {
        private final Bedwars bedwars;
        private int count = 0;

        public Countdown(Bedwars bedwars, int count) {
            this.bedwars = bedwars;
            this.count = count;
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

        private void ShowTitle() {
            for (Player plr: bedwars.getServer().getOnlinePlayers()) {
                //plr.spigot().sendMessage();
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
        }

        @Override
        public void run() {
            if (count == 0) {
                this.bedwars.getServer().getScheduler().cancelTask(this.getTaskId());
                this.bedwars.getMatchManager().Start();
            } else if (count < 0) {
                return;
            } else if (count <= 5 || count == 10 || (count % 15) == 0) {
                ShowTitle();
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
            player.teleport(respawnLoc);
        }
    }

    private MatchState matchState = MatchState.LOADING;
    private final ArrayList<Player> players = new ArrayList<>();
    private HashMap<Player, MatchPlayer> matchPlayers = null;
    private Countdown countdown = null;
    private Map map = null;
    private final PotionEffect saturation = new PotionEffect(PotionEffectType.SATURATION, 99999, 1, false, false);
    private ArrayList<Team> teams = null;

    private final Bedwars bedwars;

    public MatchManager(Bedwars bedwars) {
        this.bedwars = bedwars;
    }

    private void showScoreboard() {
        for (Player prl : players) {
            this.bedwars.getScoreboardManager().ShowScoreboard(prl);
        }
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
        if (players.size() >= this.bedwars.getSettingsManager().getMaxPlayerCount()) {
            ev.setJoinMessage("");
            plr.kickPlayer("Match is full!");
            return;
        }
        players.add(plr);

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("playerName", plr.getDisplayName());
        variables.put("playerCount", players.size());
        variables.put("maxPlayerCount", this.bedwars.getSettingsManager().getMaxPlayerCount());

        String message = this.bedwars.getConfigManager().getString("locales.en.joinMessage");
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = Evaluator.evaluate(message, variables);
        ev.setJoinMessage(message);

        if (matchState == MatchState.LOBBY && players.size() >= this.bedwars.getSettingsManager().getMinPlayerCount()) {
            StartCountdown(true);
        }

        plr.teleport(map.getLobbySpawnLoc());
        plr.getInventory().clear();
        plr.setGameMode(GameMode.SURVIVAL);
        saturation.apply(plr);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent ev) {
        Player plr = ev.getPlayer();
        if (!players.contains(plr)) return;
        players.remove(plr);

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("playerName", plr.getDisplayName());
        if (this.bedwars.getSettingsManager().getServerType() == ServerType.MATCH) {
            variables.put("playerCount", players.size());
            variables.put("maxPlayerCount", this.bedwars.getSettingsManager().getMaxPlayerCount());
        }

        String message = this.bedwars.getConfigManager().getString("locales.en.leaveMessage");
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = Evaluator.evaluate(message, variables);
        ev.setQuitMessage(message);

        if (matchState == MatchState.STARTING && players.size() < this.bedwars.getSettingsManager().getMinPlayerCount()) {
            matchState = MatchState.LOBBY;
            countdown.cancel();
            countdown = null;
        }

        if (matchState == MatchState.INGAME) {

            showScoreboard();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent ev) {
        Player plr = ev.getEntity();
        plr.setHealth(plr.getMaxHealth());
        switch (matchState) {
            case LOBBY:
            case STARTING: {
                plr.teleport(map.getLobbySpawnLoc());
                plr.setFallDistance(0);
                plr.setGameMode(GameMode.SURVIVAL);
            } break;
            case INGAME: {
                MatchPlayer matchPlayer = matchPlayers.get(plr);
                Location newLoc = map.getLobbySpawnLoc();
                newLoc.setY(newLoc.getY()+6);
                plr.teleport(newLoc);
                plr.setGameMode(GameMode.SPECTATOR);
                if (matchPlayer.getTeam().playerDied(plr)) {
                    new Respawn(plr, new Location(map.getWorld(), 0.5, 1, 0.5)).runTaskLater(this.bedwars, 20*5L);
                    new Countdown(this.bedwars, 5).runTaskTimer(this.bedwars, 0L, 20L);
                } else {
                    ev.setDeathMessage(ev.getDeathMessage() + " Final kill!");
                }
                int aliveTeams = 0;
                for (Team team : teams) {
                    if (team.isAlive()) {
                        ++aliveTeams;
                    }
                }
                showScoreboard();
                if (aliveTeams <= 1) {
                    Finish();
                }
            } break;
            case FINISH: {
                plr.teleport(new Location(map.getWorld(), 0.5, 1, 0.5));
                plr.setFallDistance(0);
                plr.setGameMode(GameMode.SURVIVAL);
                plr.setAllowFlight(true);
            }
            default: {
            }
        }
        saturation.apply(plr);
    }

    public boolean Load() {
        matchState = MatchState.LOADING;
        teams = null;
        for (Player plr : this.bedwars.getServer().getOnlinePlayers()) {
            plr.kickPlayer("Reloading lol");
        }
        this.bedwars.getMapManager().UnloadMap("flat");
        map = this.bedwars.getMapManager().LoadMap("flat");
        if (!map.isComplete()) {
            map.setLobbySpawnLoc(new Location(map.getWorld(), 0.5, 12.0, 0.5, 0.0f, 0.0f));
            map.setLobbyBound1(new Location(map.getWorld(), 4.0, 11.0, 4.0, 0.0f, 0.0f));
            map.setLobbySpawnLoc(new Location(map.getWorld(), -4.0, 15.0, -4.0, 0.0f, 0.0f));
            map.setTeams(new ArrayList<>());
        }
        map.getWorld().setGameRuleValue("doDaylightCycle", "false");
        map.getWorld().setGameRuleValue("doWeatherCycle", "false");
        matchState = MatchState.LOBBY;
        return true;
    }

    public void StartCountdown(boolean b) {
        matchState = MatchState.STARTING;
        ShowCountdown(b?30:5);
    }

    public void Start() {
        matchState = MatchState.INGAME;
        teams = new ArrayList<>();
        Stack<Player> playerStack = new Stack<>();
        for (Player plr : players) {
            plr.teleport(new Location(map.getWorld(), 0.5, 1, 0.5));
            plr.setFallDistance(0);
            playerStack.push(plr);
        }
        matchPlayers = new HashMap<>();
        for (int i = this.bedwars.getSettingsManager().getMaxTeamCount(); i > 0; --i) {
            int j = this.bedwars.getSettingsManager().getMaxTeamCount()-i;
            Team team = new Team(TeamColor.values()[j], new Location(map.getWorld(), 0, 1, 0));
            while (!team.isFull() && !playerStack.isEmpty()) {
                Player prl = playerStack.pop();
                MatchPlayer matchPrl = new MatchPlayer(prl, team);
                matchPlayers.put(prl, matchPrl);
                team.addPlayer(matchPrl);
            }
            teams.add(team);
        }
        showScoreboard();
    }

    void Finish() {
        matchState = MatchState.FINISH;
        Team winningTeam = matchPlayers.get(players.get(0)).getTeam();
        for (Team team : teams) {
            if (team.isAlive()) {
                winningTeam = team;
                break;
            }
        }
        for (Player prl : players) {
            if (winningTeam.containsPlayer(prl)) {
                prl.setAllowFlight(true);
            }
            prl.sendMessage(winningTeam.getTeamColor().getName() + " won!");
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
        return teams;
    }
}

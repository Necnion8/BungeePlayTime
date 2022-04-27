package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands.*;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.Database;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.MySQLDatabase;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeResult;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio.BungeeDataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio.ServerMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.errors.DatabaseError;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks.BTLPAFKTagVariable;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners.PlayerListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChange;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChangeRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.PingRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.SettingChange;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public final class BungeePlayTime extends Plugin implements PlayTimeAPI, BungeeDataMessenger.RequestListener {
    private static BungeePlayTime instance;
    private final MainConfig mainConfig = new MainConfig(this);
    private Database database;
    private final Map<UUID, PlayerTime> players = Maps.newConcurrentMap();
    private BungeeDataMessenger messenger;


    @Override
    public void onLoad() {
        instance = this;
        if (getProxy().getPluginManager().getPlugin("BungeeTabListPlus") != null) {
            getLogger().info("Registering...");
            try {
                BungeeTabListPlusAPI.registerVariable(this, new BTLPAFKTagVariable(this));
            } catch (Throwable e) {
                getLogger().warning("Failed to register BTLP Custom Variable: " + e.getMessage());
            }
        }
    }

    @Override
    public void onEnable() {
        // config
        mainConfig.load();

        // database
        connectDatabase();

        // init
        messenger = BungeeDataMessenger.register(this, BPTUtil.MESSAGE_CHANNEL_DATA, this);
        messenger.updateServerMessengers();

        getProxy().getPlayers().forEach(p ->
                insertPlayer(p, System.currentTimeMillis(), p.getServer().getInfo().getName(), AFKState.UNKNOWN));

        // events
//        getProxy().registerChannel(BPTUtil.MESSAGE_CHANNEL_AFK_STATE);
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));

        // commands
        new MainCommand(this).registerCommand();
        PlayTimeCommand playTimeCommand = new PlayTimeCommand(this);
        getProxy().getPluginManager().registerCommand(this, playTimeCommand);
        getProxy().getPluginManager().registerCommand(this, new PlayTimeTopCommand(playTimeCommand));
        OnlineTimeCommand onlineTimeCommand = new OnlineTimeCommand(this);
        getProxy().getPluginManager().registerCommand(this, onlineTimeCommand);
        getProxy().getPluginManager().registerCommand(this, new OnlineTimeTopCommand(onlineTimeCommand));
        getProxy().getPluginManager().registerCommand(this, new AFKPlayersCommand(this));

    }

    @Override
    public void onDisable() {
        // unload
        messenger.unregister();
        messenger = null;

        // hooks
        try {
            BTLPAFKTagVariable.unregisterFromBTLPVariable();
        } catch (Throwable e) {
            getLogger().warning("Failed to unregister BTLP Custom Variable: " + e.getMessage());
        }

//        getProxy().unregisterChannel(BPTUtil.MESSAGE_CHANNEL_AFK_STATE);

        // database
        if (!database.isClosed()) {
            try {
                database.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public BungeeDataMessenger getMessenger() {
        return messenger;
    }

    public static BungeePlayTime getInstance() {
        return instance;
    }


    private CompletableFuture<Boolean> putPlayerTime(PlayerTime pTime, long time) {
        CompletableFuture<Boolean> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                ProxiedPlayer p = pTime.getPlayer();
                database.putTime(p.getUniqueId(), p.getName(), pTime.getStartTime(), time, pTime.getServer(), pTime.getAFKState());
                f.complete(true);

            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in putPlayerTime", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    public void insertPlayer(ProxiedPlayer player, long startTime, String server, AFKState state) {
        UUID uniqueId = player.getUniqueId();

        if (players.containsKey(uniqueId)) {
            // commit
            PlayerTime playerTime = players.get(uniqueId);
            long time = Math.max(0, startTime - playerTime.getStartTime());
            putPlayerTime(playerTime, time);
        }
        players.put(uniqueId, new PlayerTime(player, startTime, server, state));
    }

    public void removePlayer(UUID player) {
        PlayerTime start = players.remove(player);
        if (start == null)
            throw new IllegalArgumentException("player not found");

        long time = System.currentTimeMillis() - start.getStartTime();
        putPlayerTime(start, time);

    }

    public boolean connectDatabase() {
        if (database != null && !database.isClosed()) {
            try {
                database.close();
            } catch (Exception ignored) {
            }
        }

        database = new MySQLDatabase(mainConfig.getMySQL(), getLogger());
        try {
            if (database.openConnection())
                database.init();
            return true;
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to database:");
            getLogger().severe("Error: " + e.getMessage());
        }
        return false;
    }

    //

    public <Res extends Response> void onRequest(ServerMessenger messenger, Request<Res> request) {
        if (request instanceof PingRequest) {
            this.messenger.addActive(messenger);

        } else if (request instanceof AFKChange) {
            AFKChange req = (AFKChange) request;
            AFKState state = req.toBungeeAFKState();
            ProxiedPlayer player = getProxy().getPlayer(req.getPlayerId());
            if (player == null) {
                getLogger().warning("Player '" + req.getPlayerId() + "' not found");
                return;
            }
            long startTime = (req.getStartTime() > 0) ? req.getStartTime() : System.currentTimeMillis();
            insertPlayer(player, startTime, messenger.getServerInfo().getName(), state);
        }

    }

    @Override
    public void onActiveMessenger(ServerMessenger messenger) {
        sendSetting(messenger);
    }

    public void sendSettingAll() {
        SettingChange setting = createSettingSend();
        messenger.actives().forEach(msg -> msg.send(setting));
    }

    public void sendSetting(ServerMessenger messenger) {
        messenger.send(createSettingSend());
    }

    private SettingChange createSettingSend() {
        return new SettingChange(
                mainConfig.getPlayers().isPlayedInUnknownState(),
                mainConfig.getPlayers().getAFKMinutes()
        );
    }

    public void sendAFKChangeRequest(ProxiedPlayer player, boolean afk) {
        ServerMessenger messenger = this.messenger.getMessenger(player.getServer().getInfo());
        if (messenger != null) {
            messenger.send(new AFKChangeRequest(player.getUniqueId(), afk));
        }
    }

    // apis

    @Override
    public Collection<PlayerTime> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    @Override
    public Optional<PlayerTime> getPlayer(UUID playerId) {
        return Optional.ofNullable(players.get(playerId));
    }


    @Override
    public CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, long afters) {
        CompletableFuture<Optional<PlayerTimeResult>> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                Optional<PlayerTimeResult> result = database.lookupTime(playerId, afters);
                f.complete(result);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupTime", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId) {
        return lookupTime(playerId, 0);
    }

    @Override
    public CompletableFuture<PlayerTimeEntries> lookupTimeTops(int count, int offset, boolean totalTime, long afters) {
        CompletableFuture<PlayerTimeEntries> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                f.complete(database.lookupTimeTops(count, offset, totalTime, afters));

            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupTimeTops", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<PlayerTimeEntries> lookupTimeTops(int count, int offset, boolean totalTime) {
        return lookupTimeTops(count, offset, totalTime, 0);
    }

    @Override
    public CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, boolean totalTime, long afters) {
        CompletableFuture<OptionalInt> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                OptionalInt ranking = database.lookupTimeRanking(playerId, totalTime, afters);
                f.complete(ranking);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupTimeRanking", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, boolean totalTime) {
        return lookupTimeRanking(playerId, totalTime, 0);
    }

    @Override
    public CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                OptionalLong firstTime = database.lookupFirstTime(playerId);
                f.complete(firstTime);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupFirstTime", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalLong> lookupLastTime(UUID playerId) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                OptionalLong lastTime = database.lookupLastTime(playerId);
                f.complete(lastTime);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupLastTime", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }


    @Override
    public CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId) {
        CompletableFuture<Optional<PlayerName>> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                f.complete(database.getPlayerName(playerId));
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception fetchPlayerName lookupTimeRanking", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerName>> fetchPlayerId(String playerName) {
        CompletableFuture<Optional<PlayerName>> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                f.complete(database.getPlayerId(playerName));
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in fetchPlayerId", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;

    }


    public Map<UUID, String> getPlayerNameCache() {
        if (database instanceof MySQLDatabase) {
            return ((MySQLDatabase) database).cachedPlayerNames();
        }
        return Collections.emptyMap();
    }



}

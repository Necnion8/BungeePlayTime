package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands.AFKPlayersCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands.MainCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.Database;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.MySQLDatabase;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.SQLiteDatabase;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio.BungeeDataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio.ServerMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.errors.DatabaseError;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks.BungeeTabListPlusVariable;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks.ConnectorPluginBridge;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners.PlayerListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners.PluginMessageListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.CommandPlatform;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandBungee;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.commands.OnlineTimeCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.commands.OnlineTimeTopCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.commands.PlayTimeCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.commands.PlayTimeTopCommand;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChange;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChangeRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.PingRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.SettingChange;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public final class BungeePlayTime extends Plugin implements PlayTimeAPI, BungeeDataMessenger.RequestListener {
    private static PlayTimeAPI api;
    private final CommandPlatform commandPlatform = new BungeeCommandPlatform();
    private final MainConfig mainConfig = new MainConfig(this);
    private Database database;
    private final Map<UUID, PlayerTime> players = Maps.newConcurrentMap();
    private BungeeDataMessenger messenger;
    private BungeeTabListPlusVariable btlpVariable;
    private final ConnectorPluginBridge connectorPluginBridge = new ConnectorPluginBridge(this);

    @Override
    public void onLoad() {
        if (getProxy().getPluginManager().getPlugin("BungeeTabListPlus") != null) {
            getLogger().info("Registering...");
            try {
                btlpVariable = BungeeTabListPlusVariable.register(this);
            } catch (Throwable e) {
                getLogger().warning("Failed to register BTLP Custom Variable: " + e.getMessage());
            }
        }
    }

    @Override
    public void onEnable() {
        api = this;

        // config
        mainConfig.load();

        // database
        connectDatabase();

        // init connector
        if (mainConfig.isConnectorPluginSupport()) {
            connectorPluginBridge.hook();
            if (connectorPluginBridge.isEnabled())
                connectorPluginBridge.registerMessenger();
        }

        // init
        messenger = BungeeDataMessenger.register(this, BPTUtil.MESSAGE_CHANNEL_DATA, this, connectorPluginBridge);
        messenger.updateServerMessengers();

        getProxy().getPlayers().forEach(p ->
                insertPlayer(p, System.currentTimeMillis(), p.getServer().getInfo().getName(), AFKState.UNKNOWN));

        // events
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        getProxy().registerChannel(BPTUtil.MESSAGE_CHANNEL_AFK_STATE);  // legacy v1.0 bridge
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));

        // commands
        new MainCommand(this).registerCommand();
        getProxy().getPluginManager().registerCommand(this, new AFKPlayersCommand(this));
        PlayTimeCommand playTimeCommand = new PlayTimeCommand(api, commandPlatform);
        CommandBungee.register(playTimeCommand, this);
        CommandBungee.register(new PlayTimeTopCommand(playTimeCommand), this);
        OnlineTimeCommand onlineTimeCommand = new OnlineTimeCommand(api, commandPlatform);
        CommandBungee.register(onlineTimeCommand, this);
        CommandBungee.register(new OnlineTimeTopCommand(onlineTimeCommand), this);

    }

    @Override
    public void onDisable() {
        // commit current
        if (database != null) {
            for (ProxiedPlayer player : getProxy().getPlayers()) {
                if (players.containsKey(player.getUniqueId())) {
                    try {
                        removePlayer(player.getUniqueId());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        players.clear();

        // unload
        if (messenger != null)
            messenger.unregister();
        messenger = null;

        getProxy().unregisterChannel(BPTUtil.MESSAGE_CHANNEL_AFK_STATE);

        // hooks
        try {
            connectorPluginBridge.unhook();
        } catch (Throwable e) {
            getLogger().warning("Failed to unhook to ConnectorPlugin: " + e.getMessage());
        }

        try {
            if (btlpVariable != null)
                btlpVariable.unregister();

        } catch (Throwable e) {
            getLogger().warning("Failed to unregister BTLP Custom Variable: " + e.getMessage());
        }

        // database
        if (!database.isClosed()) {
            try {
                database.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static PlayTimeAPI getAPI() {
        return Objects.requireNonNull(api, "Plugin is not initialized");
    }


    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public BungeeDataMessenger getMessenger() {
        return messenger;
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

        database = null;
        MainConfig.DBType dbType = mainConfig.getDatabaseType();
        switch (dbType) {
            case MYSQL: {
                database = new MySQLDatabase(mainConfig.getMySQL(), getLogger());
                break;
            }
            case SQLITE: {
                database = new SQLiteDatabase(getDataFolder(), mainConfig.getSQLite(), getLogger());
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown database type: " + dbType);
        }

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
            AFKState state = req.getAFKState();
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
        messenger.actives().forEach(this::sendSetting);
    }

    public void sendSetting(ServerMessenger messenger) {
        messenger.send(new SettingChange(
                mainConfig.getPlayers().isPlayedInUnknownState(),
                mainConfig.getPlayers().getAFKMinutes(),
                messenger.getServerInfo().getName()
        ));
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
    public CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<Optional<PlayerTimeResult>> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                Optional<PlayerTimeResult> result = database.lookupTime(playerId, options);

                if (result.isPresent() && players.containsKey(playerId))
                    players.get(playerId).addCurrentTimesTo(result.get());

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
        return lookupTime(playerId, new LookupTimeOptions());
    }

    @Override
    public CompletableFuture<PlayerTimeEntries> lookupTimeTops(LookupTimeListOptions options) {
        CompletableFuture<PlayerTimeEntries> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                PlayerTimeEntries result = database.lookupTimeTops(options);

                result.getEntries().forEach(r -> {
                    if (players.containsKey(r.getPlayerId()))
                        players.get(r.getPlayerId()).addCurrentTimesTo(r);
                });

                f.complete(result);

            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupTimeTops", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalInt> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                OptionalInt ranking = database.lookupTimeRanking(playerId, options);
                f.complete(ranking);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupTimeRanking", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId) {
        return lookupTimeRanking(playerId, new LookupTimeOptions());
    }

    @Override
    public CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                OptionalLong firstTime = database.lookupFirstTime(playerId, options);
                f.complete(firstTime);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupFirstTime", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId) {
        return lookupFirstTime(playerId, new LookupTimeOptions());
    }

    @Override
    public CompletableFuture<OptionalLong> lookupLastTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                OptionalLong lastTime = database.lookupLastTime(playerId, options);
                f.complete(lastTime);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupLastTime", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalLong> lookupLastTime(UUID playerId) {
        return lookupLastTime(playerId, new LookupTimeOptions());
    }

    @Override
    public CompletableFuture<Long> lookupPlayerCount(LookupTimeOptions options) {
        CompletableFuture<Long> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                long total = database.lookupPlayerCount(options);
                f.complete(total);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupPlayerCount", e);
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Long> lookupOnlineDays(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<Long> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                long total = database.lookupOnlineDays(playerId, options);
                f.complete(total);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in lookupOnlineDays", e);
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
        return Optional.ofNullable(database)
                .map(Database::cachedPlayerNames)
                .orElseGet(Collections::emptyMap);
    }



}

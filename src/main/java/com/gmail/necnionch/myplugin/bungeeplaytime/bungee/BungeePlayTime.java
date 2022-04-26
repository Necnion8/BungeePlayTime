package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands.*;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.Database;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.MySQLDatabase;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeResult;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.errors.DatabaseError;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks.BTLPAFKTagVariable;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners.PlayerListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners.PluginMessageListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.task.Result;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.Test;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example.ItemRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example.PingRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example.PingResponse;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

//TODO: Last Join date command


public final class BungeePlayTime extends Plugin implements PlayTimeAPI {
    private static BungeePlayTime instance;
    private final MainConfig mainConfig = new MainConfig(this);
    private Database database;
    private final Map<UUID, PlayerTime> players = Maps.newConcurrentMap();
    private Test.BungeeDataIO dataSender;


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
        mainConfig.load();

        database = new MySQLDatabase(mainConfig.getMySQL(), getLogger());
        try {
            if (database.openConnection())
                database.init();
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to database:");
            getLogger().severe("Error: " + e.getMessage());
        }

        getProxy().registerChannel(BPTUtil.MESSAGE_CHANNEL_AFK_STATE);
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));

        getProxy().getPlayers().forEach(p ->  // todo: replace waitFor check
                insertPlayer(p, System.currentTimeMillis(), p.getServer().getInfo().getName(), AFKState.UNKNOWN));

        PlayTimeCommand playTimeCommand = new PlayTimeCommand(this);
        getProxy().getPluginManager().registerCommand(this, playTimeCommand);
        getProxy().getPluginManager().registerCommand(this, new PlayTimeTopCommand(playTimeCommand));
        OnlineTimeCommand onlineTimeCommand = new OnlineTimeCommand(this);
        getProxy().getPluginManager().registerCommand(this, onlineTimeCommand);
        getProxy().getPluginManager().registerCommand(this, new OnlineTimeTopCommand(onlineTimeCommand));
        getProxy().getPluginManager().registerCommand(this, new AFKPlayersCommand(this));


        ServerInfo server = getProxy().getServerInfo("paper");
        dataSender = new Test.BungeeDataIO(this, server, "bptime:data");
        dataSender.registerHandler("ping", new PingRequest.PingRequestHandler());
        dataSender.registerHandler("ping", new PingResponse.PingResponseHandler());
        dataSender.registerHandler("item", new ItemRequest.Handler());
        getProxy().registerChannel("bptime:data");
        getProxy().getPluginManager().registerListener(this, dataSender);

        getProxy().getPluginManager().registerCommand(this, new Command("testsend") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                dataSender.send(new PingRequest((args.length >= 1) ? String.join(" ", args) : "empty")).whenComplete((ret, err) -> {
                    if (err != null) {
                        sender.sendMessage(ChatColor.RED + "err: " + err.getClass().getSimpleName());
                    } else {
                        sender.sendMessage("res: " + ret.getResponseMessage());
                    }
                });
            }
        });
    }

    @Override
    public void onDisable() {
        try {
            BTLPAFKTagVariable.unregisterFromBTLPVariable();
        } catch (Throwable e) {
            getLogger().warning("Failed to unregister BTLP Custom Variable: " + e.getMessage());
        }

        getProxy().unregisterChannel(BPTUtil.MESSAGE_CHANNEL_AFK_STATE);

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

    public static BungeePlayTime getInstance() {
        return instance;
    }


    private CompletableFuture<Result<Boolean>> putPlayerTime(PlayerTime pTime, long time) {
        CompletableFuture<Result<Boolean>> f = new CompletableFuture<>();
        Result<Boolean> r = new Result<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                ProxiedPlayer p = pTime.getPlayer();
                database.putTime(p.getUniqueId(), p.getName(), pTime.getStartTime(), time, pTime.getServer(), pTime.getAFKState());
                r.setResult(true);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Exception in putPlayerTime", e);
                r.setException(new DatabaseError(e));
            }
            f.complete(r);
        });
        return f;
    }

    public void insertPlayer(ProxiedPlayer player, long startTime, String server, AFKState state) {
        UUID uniqueId = player.getUniqueId();

        if (players.containsKey(uniqueId)) {
            // commit
            PlayerTime playerTime = players.get(uniqueId);
            long time = System.currentTimeMillis() - playerTime.getStartTime();
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


    @Override
    public String formatTimeText(long millis) {
        long offset = millis / 1000L;
        int hours = (int) offset / 3600;
        offset -= (hours * 3600);
        int minutes = (int) offset / 60;

        return ((hours > 0) ? ChatColor.GOLD.toString() + hours + ChatColor.GRAY + "時間 " : "")
                + ChatColor.GOLD + minutes + ChatColor.GRAY + "分";
    }

}

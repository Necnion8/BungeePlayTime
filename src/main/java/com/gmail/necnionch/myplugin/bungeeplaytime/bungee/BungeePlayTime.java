package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.commands.*;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.*;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.errors.DatabaseError;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks.BTLPAFKTagVariable;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners.PlayerListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners.PluginMessageListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.task.Result;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

//TODO: Last Join date command


public final class BungeePlayTime extends Plugin {
    private final MainConfig mainConfig = new MainConfig(this);
    private Database database;
    private final Map<UUID, PlayerTime> players = Maps.newConcurrentMap();


    @Override
    public void onLoad() {
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

        database = new MySQLDatabase(
                mainConfig.getAddress(),
                mainConfig.getDatabase(),
                mainConfig.getUserName(),
                mainConfig.getPassword(),
                mainConfig.getOptions(),
                getLogger()
        );
        try {
            if (database.openConnection())
                database.init();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        getProxy().registerChannel(BPTUtil.MESSAGE_CHANNEL_AFK_STATE);
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));

        getProxy().getPlayers().forEach(p ->
                insertPlayer(p, System.currentTimeMillis(), p.getServer().getInfo().getName(), false));

        getProxy().getPluginManager().registerCommand(this, new PlayTimeCommand(this));
        getProxy().getPluginManager().registerCommand(this, new PlayTimeTopCommand(this));
        getProxy().getPluginManager().registerCommand(this, new OnlineTimeCommand(this));
        getProxy().getPluginManager().registerCommand(this, new OnlineTimeTopCommand(this));
        getProxy().getPluginManager().registerCommand(this, new AFKPlayersCommand(this));

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


    private CompletableFuture<Result<Boolean>> putPlayerTime(PlayerTime pTime, long time) {
        CompletableFuture<Result<Boolean>> f = new CompletableFuture<>();
        Result<Boolean> r = new Result<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                ProxiedPlayer p = pTime.getPlayer();
                database.putTime(p.getUniqueId(), p.getName(), pTime.getStartTime(), time, pTime.getServer(), pTime.isAFK());
                r.setResult(true);
            } catch (SQLException e) {
                e.printStackTrace();
                r.setException(new DatabaseError(e));
            }
            f.complete(r);
        });
        return f;
    }

    public void insertPlayer(ProxiedPlayer player, long startTime, String server, boolean afk) {
        UUID uniqueId = player.getUniqueId();

        if (players.containsKey(uniqueId)) {
            // commit
            PlayerTime playerTime = players.get(uniqueId);
            long time = System.currentTimeMillis() - playerTime.getStartTime();
            putPlayerTime(playerTime, time);
        }
        players.put(uniqueId, new PlayerTime(player, startTime, server, afk));

    }

    public void removePlayer(UUID player) {
        PlayerTime start = players.remove(player);
        if (start == null)
            throw new IllegalArgumentException("player not found");

        long time = System.currentTimeMillis() - start.getStartTime();
        putPlayerTime(start, time);

    }


    public Collection<PlayerTime> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public Optional<PlayerTime> getPlayer(UUID playerId) {
        return Optional.ofNullable(players.get(playerId));
    }



    public CompletableFuture<Optional<LookupPlayerResult>> lookupTime(UUID playerId) {
        CompletableFuture<Optional<LookupPlayerResult>> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                Optional<LookupPlayerResult> result = database.lookupTime(playerId);
                f.complete(result);
            } catch (SQLException e) {
                e.printStackTrace();
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

//    public CompletableFuture<Optional<LookupPlayerResult>> lookupPlayTime(UUID playerId, long afters) {
//        CompletableFuture<Optional<LookupPlayerResult>> f = new CompletableFuture<>();
//
//        getProxy().getScheduler().runAsync(this, () -> {
//            try {
//                Optional<LookupPlayerResult> result = database.lookupPlayTime(playerId, afters);
//                f.complete(result);
//            } catch (SQLException e) {
//                e.printStackTrace();
//                f.completeExceptionally(new DatabaseError(e));
//            }
//        });
//        return f;
//    }


    public CompletableFuture<LookupTop> lookupTimeTops(int lookupCount, boolean afks) {
        CompletableFuture<LookupTop> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                f.complete(database.lookupTimeTops(lookupCount, afks));

            } catch (SQLException e) {
                e.printStackTrace();
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

//    public CompletableFuture<LookupTop> lookupTops(int lookupCount, boolean afks, long afters) {
//        CompletableFuture<LookupTop> f = new CompletableFuture<>();
//
//        getProxy().getScheduler().runAsync(this, () -> {
//            try {
//                f.complete(database.lookupTops(lookupCount, afks, afters));
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//                f.completeExceptionally(new DatabaseError(e));
//            }
//        });
//        return f;
//    }


    public CompletableFuture<Optional<PlayerId>> fetchPlayerName(UUID playerId) {
        CompletableFuture<Optional<PlayerId>> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                f.complete(database.getPlayerName(playerId));
            } catch (SQLException e) {
                e.printStackTrace();
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;
    }

    public CompletableFuture<Optional<PlayerId>> fetchPlayerId(String playerName) {
        CompletableFuture<Optional<PlayerId>> f = new CompletableFuture<>();

        getProxy().getScheduler().runAsync(this, () -> {
            try {
                f.complete(database.getPlayerId(playerName));
            } catch (SQLException e) {
                e.printStackTrace();
                f.completeExceptionally(new DatabaseError(e));
            }
        });
        return f;

    }



    public String formatTimeText(long millis) {
        long offset = millis / 1000L;
        int hours = (int) offset / 3600;
        offset -= (hours * 3600);
        int minutes = (int) offset / 60;

        return ((hours > 0) ? ChatColor.GOLD.toString() + hours + ChatColor.GRAY + "時間 " : "")
                + ChatColor.GOLD + minutes + ChatColor.GRAY + "分";
    }

}

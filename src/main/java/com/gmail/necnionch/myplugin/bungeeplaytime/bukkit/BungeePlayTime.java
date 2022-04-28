package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio.BukkitDataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.hooks.AFKPlusBridge;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.listeners.PlayerListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class BungeePlayTime extends JavaPlugin implements PlayTimeAPI {
    private BukkitDataMessenger messenger;
    private final AFKPlusBridge afkPlusBridge = new AFKPlusBridge(this);
    private int afkMinutes = 5;
    private boolean bungeeConnected;

    @Override
    public void onEnable() {
        // events
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // init
        messenger = BukkitDataMessenger.register(this, BPTUtil.MESSAGE_CHANNEL_DATA, this::onRequest);
        getServer().getScheduler().runTask(this, () -> {
            if (!getServer().getOnlinePlayers().isEmpty())
                messenger.send(new PingRequest()).whenComplete((ret, err) -> {
                    if (err == null)
                        onConnect();
                });
        });

        // hooks
        if (afkPlusBridge.hook())
            getLogger().info("Hooked to AFKPlus");

    }

    @Override
    public void onDisable() {
        messenger.unregister();
        messenger = null;

        afkPlusBridge.unhook();
    }

    public BukkitDataMessenger getMessenger() {
        return messenger;
    }

    public int getAFKMinutes() {
        return afkMinutes;
    }

    public boolean isBungeeConnected() {
        return bungeeConnected;
    }

    public void setBungeeConnected(boolean bungeeConnected) {
        this.bungeeConnected = bungeeConnected;
    }


    private void onConnect() {
        bungeeConnected = true;
        getServer().getOnlinePlayers()
                .forEach(p -> {
                    AFKState state = AFKState.UNKNOWN;
                    if (afkPlusBridge.isEnabled())
                        state = afkPlusBridge.isAFK(p) ? AFKState.TRUE : AFKState.FALSE;
                    getMessenger().send(new AFKChange(p.getUniqueId(), state));
                });
    }


    private <Res extends Response> void onRequest(Request<Res> request) {
        if (request instanceof PingRequest) {
            onConnect();
        } else if (request instanceof AFKChangeRequest) {
            AFKChangeRequest req = (AFKChangeRequest) request;
            if (afkPlusBridge.isEnabled()) {
                afkPlusBridge.setAFK(req.getPlayerId(), req.isAFK());
            }
        } else if (request instanceof SettingChange) {
            SettingChange req = (SettingChange) request;
            BPTUtil.setPlayedInUnknownState(req.isPlayedInUnknown());
            afkMinutes = req.getAFKMinutes();
        }

    }


    @Override
    public CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<Optional<PlayerTimeResult>> f = new CompletableFuture<>();
        messenger.send(new GetPlayerTimeRequest(playerId, options)).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId) {
        return lookupTime(playerId, new LookupTimeListOptions());
    }

    @Override
    public CompletableFuture<PlayerTimeEntries> lookupTimeTops(LookupTimeListOptions options) {
        CompletableFuture<PlayerTimeEntries> f = new CompletableFuture<>();
        messenger.send(new GetPlayerTimeEntriesRequest(options)).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalInt> f = new CompletableFuture<>();
        messenger.send(new GetPlayerTimeRankingRequest(playerId, options)).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getRanking());
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();
        messenger.send(new GetPlayerFirstTimeRequest(playerId, options)).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getFirstTime());
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalLong> lookupLastTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();
        messenger.send(new GetPlayerLastTimeRequest(playerId, options)).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getLastTime());
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId) {
        CompletableFuture<Optional<PlayerName>> f = new CompletableFuture<>();
        messenger.send(new GetPlayerNameRequest(playerId, null)).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerName>> fetchPlayerId(String playerName) {
        CompletableFuture<Optional<PlayerName>> f = new CompletableFuture<>();
        messenger.send(new GetPlayerNameRequest(null, playerName)).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
            }
        });
        return f;
    }

}

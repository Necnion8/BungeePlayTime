package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio.BukkitDataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.hooks.AFKPlusBridge;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.listeners.PlayerListener;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.CommandPlatform;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandBukkit;
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
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public class BungeePlayTime extends JavaPlugin implements PlayTimeAPI {
    private static PlayTimeAPI api;
    private final CommandPlatform commandPlatform = new BukkitCommandPlatform();
    private BukkitDataMessenger messenger;
    private final AFKPlusBridge afkPlusBridge = new AFKPlusBridge(this);
    private int afkMinutes = 5;
    private boolean bungeeConnected;
    private String currentServerName;

    @Override
    public void onEnable() {
        api = this;
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

        // register commands
        PlayTimeCommand playTimeCommand = new PlayTimeCommand(api, commandPlatform);
        CommandBukkit.register(playTimeCommand);
        CommandBukkit.register(new PlayTimeTopCommand(playTimeCommand));
        OnlineTimeCommand onlineTimeCommand = new OnlineTimeCommand(api, commandPlatform);
        CommandBukkit.register(onlineTimeCommand);
        CommandBukkit.register(new OnlineTimeTopCommand(onlineTimeCommand));

    }

    @Override
    public void onDisable() {
        messenger.unregister();
        messenger = null;

        afkPlusBridge.unhook();
    }

    public static PlayTimeAPI getAPI() {
        return Objects.requireNonNull(api, "Plugin is not initialized");
    }


    public BukkitDataMessenger getMessenger() {
        return messenger;
    }

    public int getAFKMinutes() {
        return afkMinutes;
    }

    public void onEmptyPlayers() {
        bungeeConnected = false;
        currentServerName = null;
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
        bungeeConnected = true;
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
            currentServerName = req.getServerName();
        }

    }


    @Override
    public boolean isBungeeConnected() {
        return bungeeConnected;
    }

    @Override
    public String getServerNameInBungee() {
        return currentServerName;
    }

    private LookupTimeOptions setCurrentServerFrom(LookupTimeOptions options) {
        if (options.isCurrentServer())
            return options.copyTo(null).server(currentServerName != null ? currentServerName : "");
        return options;
    }

    private LookupTimeListOptions setCurrentServerFrom(LookupTimeListOptions options) {
        if (options.isCurrentServer())
            return options.copyTo(null).server(currentServerName != null ? currentServerName : "");
        return options;
    }


    @Override
    public CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<Optional<PlayerTimeResult>> f = new CompletableFuture<>();
        messenger.send(new GetPlayerTimeRequest(playerId, setCurrentServerFrom(options)), 3000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
                bungeeConnected = true;
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId) {
        return lookupTime(playerId, setCurrentServerFrom(new LookupTimeOptions().currentServer()));
    }

    @Override
    public CompletableFuture<PlayerTimeEntries> lookupTimeTops(LookupTimeListOptions options) {
        CompletableFuture<PlayerTimeEntries> f = new CompletableFuture<>();
        messenger.send(new GetPlayerTimeEntriesRequest(setCurrentServerFrom(options)), 5000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
                bungeeConnected = true;
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalInt> f = new CompletableFuture<>();
        messenger.send(new GetPlayerTimeRankingRequest(playerId, setCurrentServerFrom(options)), 3000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getRanking());
                bungeeConnected = true;
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId) {
        return lookupTimeRanking(playerId, setCurrentServerFrom(new LookupTimeListOptions().currentServer()));
    }

    @Override
    public CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();
        messenger.send(new GetPlayerFirstTimeRequest(playerId, setCurrentServerFrom(options)), 3000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getFirstTime());
                bungeeConnected = true;
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId) {
        return lookupFirstTime(playerId, setCurrentServerFrom(new LookupTimeListOptions().currentServer()));
    }

    @Override
    public CompletableFuture<OptionalLong> lookupLastTime(UUID playerId, LookupTimeOptions options) {
        CompletableFuture<OptionalLong> f = new CompletableFuture<>();
        messenger.send(new GetPlayerLastTimeRequest(playerId, setCurrentServerFrom(options)), 3000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getLastTime());
                bungeeConnected = true;
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<OptionalLong> lookupLastTime(UUID playerId) {
        return lookupLastTime(playerId, setCurrentServerFrom(new LookupTimeOptions().currentServer()));
    }

    @Override
    public CompletableFuture<Long> lookupPlayerCount(LookupTimeOptions options) {
        CompletableFuture<Long> f = new CompletableFuture<>();
        messenger.send(new GetPlayerCountRequest(setCurrentServerFrom(options)), 3000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getTotal());
                bungeeConnected = true;
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId) {
        CompletableFuture<Optional<PlayerName>> f = new CompletableFuture<>();
        messenger.send(new GetPlayerNameRequest(playerId, null), 3000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
                bungeeConnected = true;
            }
        });
        return f;
    }

    @Override
    public CompletableFuture<Optional<PlayerName>> fetchPlayerId(String playerName) {
        CompletableFuture<Optional<PlayerName>> f = new CompletableFuture<>();
        messenger.send(new GetPlayerNameRequest(null, playerName), 3000).whenComplete((ret, err) -> {
            if (err != null) {
                f.completeExceptionally(err);
            } else {
                f.complete(ret.getResult());
                bungeeConnected = true;
            }
        });
        return f;
    }

}

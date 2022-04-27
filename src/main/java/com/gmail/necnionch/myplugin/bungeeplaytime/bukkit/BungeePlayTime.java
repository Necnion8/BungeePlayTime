package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio.BukkitDataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.hooks.AFKPlusBridge;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChange;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChangeRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.PingRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.SettingChange;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class BungeePlayTime extends JavaPlugin implements Listener {
    private static BungeePlayTime instance;
    private BukkitDataMessenger messenger;
    private final AFKPlusBridge afkPlusBridge = new AFKPlusBridge(this);
    private boolean playedInUnknownState;
    private int afkMinutes = 5;

    @Override
    public void onEnable() {
        instance = this;
        // events
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, BPTUtil.MESSAGE_CHANNEL_AFK_STATE);

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

    public static BungeePlayTime getInstance() {
        return instance;
    }

    public BukkitDataMessenger getMessenger() {
        return messenger;
    }

    public boolean isPlayedInUnknownState() {
        return playedInUnknownState;
    }

    public int getAFKMinutes() {
        return afkMinutes;
    }

    private void onConnect() {
        getServer().getOnlinePlayers()
                .forEach(p -> {
                    AFKState state = AFKState.UNKNOWN;
                    if (afkPlusBridge.isEnabled())
                        state = afkPlusBridge.isAFK(p) ? AFKState.TRUE : AFKState.FALSE;
                    getMessenger().send(new AFKChange(p.getUniqueId(), state.getValue()));
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
            playedInUnknownState = req.isPlayedInUnknown();
            afkMinutes = req.getAFKMinutes();
        }

    }

}

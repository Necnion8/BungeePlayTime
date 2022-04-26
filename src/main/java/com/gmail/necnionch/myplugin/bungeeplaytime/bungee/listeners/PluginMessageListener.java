package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.data.AFKState;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {

    private final BungeePlayTime owner;

    public PluginMessageListener(BungeePlayTime owner) {
        this.owner = owner;
    }


    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if (!BPTUtil.MESSAGE_CHANNEL_AFK_STATE.equals(event.getTag()))
            return;

        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
        AFKState state = AFKState.deserialize(event.getData());
        com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState afkState = com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState.valueOf(state.isAfk() ? 1 : 0);
        owner.insertPlayer(player, System.currentTimeMillis(), player.getServer().getInfo().getName(), afkState);

    }

}

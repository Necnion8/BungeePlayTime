package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {

    private final BungeePlayTime plugin;

    public PluginMessageListener(BungeePlayTime plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onReceive(PluginMessageEvent event) {
        if (BPTUtil.MESSAGE_CHANNEL_AFK_STATE.equals(event.getTag())) {
            ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
            plugin.insertPlayer(player, System.currentTimeMillis(), player.getServer().getInfo().getName(), AFKState.deserializeFromLegacy(event.getData()));
        }
    }

}

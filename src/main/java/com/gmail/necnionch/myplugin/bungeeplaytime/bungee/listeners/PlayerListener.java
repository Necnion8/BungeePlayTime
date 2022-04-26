package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;


public class PlayerListener implements Listener {
    private final BungeePlayTime plugin;

    public PlayerListener(BungeePlayTime plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onSwitch(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo fromServer = (player.getServer() != null) ? player.getServer().getInfo() : null;
        ServerInfo toServer = event.getServer().getInfo();


        if (fromServer == null) {  // proxy join
            plugin.insertPlayer(player, System.currentTimeMillis(), toServer.getName(), AFKState.FALSE);

        } else {  // switched
            plugin.insertPlayer(player, System.currentTimeMillis(), toServer.getName(), AFKState.FALSE);

        }
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player.getServer() != null) {
            plugin.removePlayer(player.getUniqueId());
        }

    }



}

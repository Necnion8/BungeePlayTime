package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.listeners;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Set;


public class PlayerListener implements Listener {
    private final BungeePlayTime plugin;
    private final Set<ProxiedPlayer> joinedPlayers = Sets.newHashSet();

    public PlayerListener(BungeePlayTime plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onSwitch(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo fromServer = (player.getServer() != null) ? player.getServer().getInfo() : null;
        ServerInfo toServer = event.getServer().getInfo();

        joinedPlayers.add(player);

        plugin.insertPlayer(player, System.currentTimeMillis(), toServer.getName(), AFKState.UNKNOWN);
        if (fromServer != null && fromServer.getPlayers().size() <= 1) {
            plugin.getMessenger().removeActive(fromServer);
        }
        plugin.getMessenger().sendPing(toServer);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!joinedPlayers.remove(player))  // 即抜け対応
            return;

        plugin.removePlayer(player.getUniqueId());

        if (player.getServer() != null) {
            if (player.getServer().getInfo().getPlayers().size() <= 1) {
                plugin.getMessenger().removeActive(player.getServer().getInfo());
            }
        }

    }

    @EventHandler
    public void onChat(ChatEvent event) {  // chat or proxy command executes
        if (!(event.getSender() instanceof ProxiedPlayer))
            return;

        if (event.isCommand() && !event.isProxyCommand())
            return;  // ignored non-proxy command

        plugin.sendAFKChangeRequest(((ProxiedPlayer) event.getSender()), false);

    }

}

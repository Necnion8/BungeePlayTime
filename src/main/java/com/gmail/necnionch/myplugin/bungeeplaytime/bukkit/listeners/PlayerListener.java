package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.listeners;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.BungeePlayTime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final BungeePlayTime plugin;

    public PlayerListener(BungeePlayTime plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.onEmptyPlayers();
    }

}

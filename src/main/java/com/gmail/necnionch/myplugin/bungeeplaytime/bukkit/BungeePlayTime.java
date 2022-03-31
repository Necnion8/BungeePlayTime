package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;


public class BungeePlayTime extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, BPTUtil.MESSAGE_CHANNEL_AFK_STATE);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStart(AFKStartEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        AFKState state = new AFKState(uuid, true);
        player.sendPluginMessage(this, BPTUtil.MESSAGE_CHANNEL_AFK_STATE, state.serialize());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStop(AFKStopEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        AFKState state = new AFKState(uuid, false);
        player.sendPluginMessage(this, BPTUtil.MESSAGE_CHANNEL_AFK_STATE, state.serialize());
    }

}

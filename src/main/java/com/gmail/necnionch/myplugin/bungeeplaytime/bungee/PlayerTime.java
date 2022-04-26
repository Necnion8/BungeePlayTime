package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;


public class PlayerTime {
    private final ProxiedPlayer player;
    private final long startTime;
    private final String server;
    private final AFKState afk;


    public PlayerTime(ProxiedPlayer player, long startTime, String server, AFKState state) {
        this.player = player;
        this.startTime = startTime;
        this.server = server;
        this.afk = state;
    }


    public ProxiedPlayer getPlayer() {
        return player;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getServer() {
        return server;
    }

    public boolean isAFK() {
        return !afk.isPlayed();
    }

    public AFKState getAFKState() {
        return afk;
    }

}

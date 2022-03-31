package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;


public class PlayerTime {
    private final ProxiedPlayer player;
    private final long startTime;
    private final String server;
    private final boolean afk;


    public PlayerTime(ProxiedPlayer player, long startTime, String server, boolean afk) {
        this.player = player;
        this.startTime = startTime;
        this.server = server;
        this.afk = afk;
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
        return afk;
    }

}

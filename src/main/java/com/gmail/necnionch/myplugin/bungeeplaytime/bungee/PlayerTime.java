package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;
import net.md_5.bungee.api.connection.ProxiedPlayer;


public class PlayerTime {
    private final ProxiedPlayer player;
    private final long startTime;
    private final String server;
    private final AFKState afkState;


    public PlayerTime(ProxiedPlayer player, long startTime, String server, AFKState state) {
        this.player = player;
        this.startTime = startTime;
        this.server = server;
        this.afkState = state;
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
        return !afkState.isPlayed();
    }

    public AFKState getAFKState() {
        return afkState;
    }


    public void addCurrentTimesTo(PlayerTimeResult result) {
        switch (afkState) {
            case TRUE:
                result.setAFKTime(result.getAFKTime() + (System.currentTimeMillis() - startTime));
                break;
            case FALSE:
                result.setPlayTime(result.getPlayTime() + (System.currentTimeMillis() - startTime));
                break;
            case UNKNOWN:
                result.setUnknownTime(result.getUnknownTime() + (System.currentTimeMillis() - startTime));
                break;
            default:
                throw new IllegalArgumentException("Unknown AFKState: " + afkState);
        }
    }

}

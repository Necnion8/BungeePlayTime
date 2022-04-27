package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;

import java.util.UUID;

public class PlayerTimeResult {

    private final UUID playerId;
    private final long playedMillis;
    private final long afkMillis;
    private final long unknownMillis;

    public PlayerTimeResult(UUID playerId, long playedMillis, long afkMillis, long unknownMillis) {
        this.playerId = playerId;
        this.playedMillis = playedMillis;
        this.afkMillis = afkMillis;
        this.unknownMillis = unknownMillis;
    }

    public UUID getPlayerId() {
        return playerId;
    }


    public long getTotalTime() {
        return playedMillis + afkMillis + unknownMillis;
    }

    public long getPlayTime() {
        if (AFKState.isPlayedInUnknownState())
            return playedMillis + unknownMillis;
        return playedMillis;
    }

    public long getAFKTime() {
        return afkMillis;
    }

}

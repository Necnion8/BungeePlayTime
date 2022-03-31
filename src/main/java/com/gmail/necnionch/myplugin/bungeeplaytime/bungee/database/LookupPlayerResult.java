package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import java.util.UUID;

public class LookupPlayerResult {

    private final UUID playerId;
    private final long playedMillis;
    private final long afkMillis;

    public LookupPlayerResult(UUID playerId, long playedMillis, long afkMillis) {
        this.playerId = playerId;
        this.playedMillis = playedMillis;
        this.afkMillis = afkMillis;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getPlayedMillis() {
        return playedMillis;
    }

    public long getAFKMillis() {
        return afkMillis;
    }

}

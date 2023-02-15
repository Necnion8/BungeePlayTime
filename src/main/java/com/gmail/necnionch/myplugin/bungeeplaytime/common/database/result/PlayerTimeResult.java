package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public class PlayerTimeResult {

    private final UUID playerId;
    private long playedMillis;
    private long afkMillis;
    private long unknownMillis;

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

    public long getUnknownTime() {
        return unknownMillis;
    }

    public void setPlayTime(long millis) {
        this.playedMillis = millis;
    }

    public void setAFKTime(long millis) {
        this.afkMillis = millis;
    }

    public void setUnknownTime(long millis) {
        this.unknownMillis = millis;
    }


    public void serializeTo(ByteArrayDataOutput output) {
        output.writeUTF(playerId.toString());
        output.writeLong(playedMillis);
        output.writeLong(afkMillis);
        output.writeLong(unknownMillis);
    }

    public static PlayerTimeResult deserializeFrom(ByteArrayDataInput input) {
        return new PlayerTimeResult(UUID.fromString(input.readUTF()), input.readLong(), input.readLong(), input.readLong());
    }

}

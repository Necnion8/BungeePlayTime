package com.gmail.necnionch.myplugin.bungeeplaytime.common;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

public class AFKState {

    private final UUID player;
    private final boolean afk;

    public AFKState(UUID player, boolean afk) {
        this.player = player;
        this.afk = afk;
    }

    public UUID getPlayer() {
        return player;
    }

    public boolean isAfk() {
        return afk;
    }


    @SuppressWarnings("UnstableApiUsage")
    public static AFKState deserialize(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        return new AFKState(UUID.fromString(in.readUTF()), in.readBoolean());
    }

    @SuppressWarnings("UnstableApiUsage")
    public byte[] serialize() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(player.toString());
        out.writeBoolean(afk);
        return out.toByteArray();
    }

}

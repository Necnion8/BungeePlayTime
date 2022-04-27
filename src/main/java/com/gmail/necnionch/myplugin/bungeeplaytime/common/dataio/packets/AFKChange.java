package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public class AFKChange extends Request<AFKChangeResponse> {
    public static final String KEY = "afk_change";
    private final UUID playerId;
    private final int afkState;
    private final long startTime;

    public AFKChange(UUID playerId, int afkState) {
        this.playerId = playerId;
        this.afkState = afkState;
        this.startTime = -1;
    }

    public AFKChange(UUID playerId, int afkState, long startTime) {
        this.playerId = playerId;
        this.afkState = afkState;
        this.startTime = startTime;
    }

    public static AFKChange fromBukkit(UUID playerId, AFKState state) {
        return new AFKChange(playerId, state.getValue());
    }

    public static AFKChange fromBukkit(UUID playerId, AFKState state, long startTime) {
        return new AFKChange(playerId, state.getValue(), startTime);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState toBungeeAFKState() {
        return com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState.valueOrNoneOf(afkState);
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(playerId.toString());
        output.writeInt(afkState);
        output.writeLong(startTime);
    }


    public static class Handler extends RequestHandler<AFKChange, AFKChangeResponse> {
        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public AFKChange handleRequest(ByteArrayDataInput input) {
            return new AFKChange(UUID.fromString(input.readUTF()), input.readInt(), input.readLong());
        }

        @Override
        public AFKChangeResponse processRequest(AFKChange request) {
            return new AFKChangeResponse();
        }
    }

}

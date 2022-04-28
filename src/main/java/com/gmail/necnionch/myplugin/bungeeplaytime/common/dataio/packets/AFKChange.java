package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AFKChange extends Request<AFKChangeResponse> {
    public static final String KEY = "afk_change";
    private final UUID playerId;
    private final long startTime;
    private final AFKState afkState;

    public AFKChange(UUID playerId, AFKState state) {
        this.playerId = playerId;
        this.afkState = state;
        this.startTime = -1;
    }

    public AFKChange(UUID playerId, AFKState state, long startTime) {
        this.playerId = playerId;
        this.afkState = state;
        this.startTime = startTime;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getStartTime() {
        return startTime;
    }

    public AFKState getAFKState() {
        return afkState;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(playerId.toString());
        output.writeInt(afkState.getValue());
        output.writeLong(startTime);
    }


    public static class Handler extends RequestHandler<AFKChange, AFKChangeResponse> {
        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public AFKChange handleRequest(ByteArrayDataInput input) {
            return new AFKChange(UUID.fromString(input.readUTF()), AFKState.valueOrNoneOf(input.readInt()), input.readLong());
        }

        @Override
        public CompletableFuture<AFKChangeResponse> processRequest(AFKChange request) {
            return CompletableFuture.completedFuture(new AFKChangeResponse());
        }
    }

}

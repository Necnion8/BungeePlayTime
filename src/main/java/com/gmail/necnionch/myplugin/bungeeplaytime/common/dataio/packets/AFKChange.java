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

    public AFKChange(UUID playerId, int afkState) {
        this.playerId = playerId;
        this.afkState = afkState;
    }

    public static AFKChange fromBukkit(UUID playerId, AFKState state) {
        return new AFKChange(playerId, state.getValue());
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState toBungeeAFKState() {
        return com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState.valueOrNoneOf(afkState);
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(playerId.toString());
        output.writeInt(afkState);
    }


    public static class Handler extends RequestHandler<AFKChange, AFKChangeResponse> {
        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public AFKChange handleRequest(ByteArrayDataInput input) {
            return new AFKChange(UUID.fromString(input.readUTF()), input.readInt());
        }

        @Override
        public AFKChangeResponse processRequest(AFKChange request) {
            return new AFKChangeResponse();
        }
    }

}

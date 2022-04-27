package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;

public class AFKChangeRequest extends Request<AFKChangeResponse> {
    public static final String KEY = "afk_change_request";
    private final boolean afk;
    private final UUID playerId;

    public AFKChangeRequest(UUID playerId, boolean afk) {
        this.playerId = playerId;
        this.afk = afk;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean isAFK() {
        return afk;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(playerId.toString());
        output.writeBoolean(afk);
    }


    public static class Handler extends RequestHandler<AFKChangeRequest, AFKChangeResponse> {

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public AFKChangeRequest handleRequest(ByteArrayDataInput input) {
            return new AFKChangeRequest(UUID.fromString(input.readUTF()), input.readBoolean());
        }

        @Override
        public AFKChangeResponse processRequest(AFKChangeRequest request) {
            return new AFKChangeResponse();
        }
    }

}

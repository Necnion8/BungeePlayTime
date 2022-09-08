package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.concurrent.CompletableFuture;

public class SettingChange extends Request<SettingChangeResponse> {
    public static final String KEY = "setting_change";
    private final boolean playedInUnknown;
    private final int afkMinutes;
    private final String serverName;

    public SettingChange(boolean playedInUnknown, int afkMinutes, String serverName) {
        this.playedInUnknown = playedInUnknown;
        this.afkMinutes = afkMinutes;
        this.serverName = serverName;
    }

    public boolean isPlayedInUnknown() {
        return playedInUnknown;
    }

    public int getAFKMinutes() {
        return afkMinutes;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeBoolean(playedInUnknown);
        output.writeInt(afkMinutes);
        output.writeUTF(serverName);
    }


    public static class Handler extends RequestHandler<SettingChange, SettingChangeResponse> {

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public SettingChange handleRequest(ByteArrayDataInput input) {
            return new SettingChange(input.readBoolean(), input.readInt(), input.readUTF());
        }

        @Override
        public CompletableFuture<SettingChangeResponse> processRequest(SettingChange request) {
            return CompletableFuture.completedFuture(new SettingChangeResponse());
        }
    }

}

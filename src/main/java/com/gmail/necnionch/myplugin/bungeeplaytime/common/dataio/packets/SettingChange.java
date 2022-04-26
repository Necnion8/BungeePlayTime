package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class SettingChange extends Request<SettingChangeResponse> {
    public static final String KEY = "setting_change";
    private final boolean playedInUnknown;

    public SettingChange(boolean playedInUnknown) {
        this.playedInUnknown = playedInUnknown;
    }

    public boolean isPlayedInUnknown() {
        return playedInUnknown;
    }


    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeBoolean(playedInUnknown);
    }


    public static class Handler extends RequestHandler<SettingChange, SettingChangeResponse> {

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public SettingChange handleRequest(ByteArrayDataInput input) {
            return new SettingChange(input.readBoolean());
        }

        @Override
        public SettingChangeResponse processRequest(SettingChange request) {
            return new SettingChangeResponse();
        }
    }

}

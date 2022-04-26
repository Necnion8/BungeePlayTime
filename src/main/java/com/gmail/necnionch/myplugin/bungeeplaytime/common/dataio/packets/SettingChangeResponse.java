package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class SettingChangeResponse extends Response {
    @Override
    public String getDataKey() {
        return SettingChange.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {}


    public static class Handler extends ResponseHandler<SettingChangeResponse> {

        @Override
        public String getDataKey() {
            return SettingChange.KEY;
        }

        @Override
        public SettingChangeResponse handleResponse(ByteArrayDataInput input) {
            return new SettingChangeResponse();
        }

    }

}

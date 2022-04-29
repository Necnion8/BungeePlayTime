package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class AFKChangeResponse extends Response {
    @Override
    public String getDataKey() {
        return AFKChange.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {}


    public static class Handler extends ResponseHandler<AFKChangeResponse> {
        @Override
        public String getDataKey() {
            return AFKChange.KEY;
        }

        @Override
        public AFKChangeResponse handleResponse(ByteArrayDataInput input) {
            return new AFKChangeResponse();
        }

    }

}

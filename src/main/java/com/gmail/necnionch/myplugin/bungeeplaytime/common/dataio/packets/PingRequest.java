package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class PingRequest extends Request<PingResponse> {
    public static final String KEY = "ping";

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {}


    public static class Handler extends ResponseHandler<PingResponse> {

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public PingResponse handleResponse(ByteArrayDataInput input) {
            return new PingResponse();
        }

    }


}

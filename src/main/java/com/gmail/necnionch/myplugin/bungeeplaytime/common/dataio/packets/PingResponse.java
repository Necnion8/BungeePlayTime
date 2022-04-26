package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class PingResponse extends Response {
    @Override
    public String getDataKey() {
        return PingRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {}


    public static class Handler extends RequestHandler<PingRequest, PingResponse> {

        @Override
        public String getDataKey() {
            return PingRequest.KEY;
        }

        @Override
        public PingRequest handleRequest(ByteArrayDataInput input) {
            return new PingRequest();
        }

        @Override
        public PingResponse processRequest(PingRequest request) {
            return new PingResponse();
        }

    }

}

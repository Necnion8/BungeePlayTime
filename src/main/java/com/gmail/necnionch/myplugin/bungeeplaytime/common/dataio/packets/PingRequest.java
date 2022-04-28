package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.concurrent.CompletableFuture;

public class PingRequest extends Request<PingResponse> {
    public static final String KEY = "ping";

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {}


    public static class Handler extends RequestHandler<PingRequest, PingResponse> {

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public PingRequest handleRequest(ByteArrayDataInput input) {
            return new PingRequest();
        }

        @Override
        public CompletableFuture<PingResponse> processRequest(PingRequest request) {
            return CompletableFuture.completedFuture(new PingResponse());
        }

    }


}

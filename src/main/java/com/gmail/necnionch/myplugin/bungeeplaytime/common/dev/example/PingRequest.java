package com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class PingRequest extends Request<PingResponse> {

    private final String message;

    public PingRequest(String message) {
        this.message = message;
    }


    @Override
    public String getDataKey() {
        return "ping";
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(message);
    }

    public String getMessage() {
        return message;
    }

    public static class PingRequestHandler extends RequestHandler<PingRequest, PingResponse> {
        @Override
        public PingRequest handleRequest(ByteArrayDataInput input) {
            return new PingRequest(input.readUTF());
        }

        @Override
        public PingResponse processRequest(PingRequest request) {
            return new PingResponse("Thanks received -> " + request.message);
        }

    }

}

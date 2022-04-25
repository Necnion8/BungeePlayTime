package com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class PingResponse extends Response {

    private final String message;

    public PingResponse(String returnMessage) {
        message = returnMessage;
    }

    @Override
    public String getDataKey() {
        return "ping";
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(message);
    }

    public String getResponseMessage() {
        return message;
    }

    public static class PingResponseHandler extends ResponseHandler<PingResponse> {

        @Override
        public PingResponse handleResponse(ByteArrayDataInput input) {
            return new PingResponse(input.readUTF());
        }

    }

}

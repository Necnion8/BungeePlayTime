package com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class ItemResponse extends Response {

    private final String extra;

    public ItemResponse(String extra) {
        this.extra = extra;
    }

    @Override
    public String getDataKey() {
        return "item";
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(extra);
    }

    public String getExtra() {
        return extra;
    }

    public static class Handler extends ResponseHandler<ItemResponse> {
        @Override
        public ItemResponse handleResponse(ByteArrayDataInput input) {
            return new ItemResponse(input.readUTF());
        }
    }

}

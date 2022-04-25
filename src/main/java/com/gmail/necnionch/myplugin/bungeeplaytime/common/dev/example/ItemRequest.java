package com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.Locale;

public class ItemRequest extends Request<ItemResponse> {

    private final String material;
    private final int amount;

    public ItemRequest(String material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    @Override
    public String getDataKey() {
        return "item";
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(material);
        output.writeInt(amount);
    }


    public static class Handler extends RequestHandler<ItemRequest, ItemResponse> {

        @Override
        public ItemRequest handleRequest(ByteArrayDataInput input) {
            return new ItemRequest(input.readUTF(), input.readInt());
        }

        @Override
        public ItemResponse processRequest(ItemRequest request) {
            String extra;
            switch (request.material.toLowerCase(Locale.ROOT)) {
                case "stone":
                    extra = "Stoooone!";
                    break;
                case "diamond_sword":
                    extra = "Swoooooord!";
                    break;
                default:
                    extra = "what?";
            }
            System.out.println("hit process item : " + request.material + " x" + request.amount);
            if (true)
                throw new IllegalArgumentException("AAA");
            return new ItemResponse(extra);
        }

    }


}

package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.concurrent.CompletableFuture;

public class GetPlayerCountRequest extends Request<GetPlayerCountResponse> {
    public static final String KEY = "get_player_count";
    private final LookupTimeOptions options;

    public GetPlayerCountRequest(LookupTimeOptions options) {
        this.options = options;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        options.serializeTo(output);
    }


    public static class Handler extends RequestHandler<GetPlayerCountRequest, GetPlayerCountResponse> {

        private final PlayTimeAPI api;

        public Handler(PlayTimeAPI api) {
            this.api = api;
        }

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public GetPlayerCountRequest handleRequest(ByteArrayDataInput input) {
            return new GetPlayerCountRequest(LookupTimeOptions.deserializeFrom(input));
        }

        @Override
        public CompletableFuture<GetPlayerCountResponse> processRequest(GetPlayerCountRequest request) {
            return api.lookupPlayerCount(request.options)
                    .thenApply(GetPlayerCountResponse::new);
        }

    }
}

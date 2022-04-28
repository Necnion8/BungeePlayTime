package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.concurrent.CompletableFuture;

public class GetPlayerTimeEntriesRequest extends Request<GetPlayerTimeEntriesResponse> {
    public static final String KEY = "get_player_time_entries";
    private final LookupTimeListOptions options;

    public GetPlayerTimeEntriesRequest(LookupTimeListOptions options) {
        this.options = options;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    public LookupTimeListOptions getOptions() {
        return options;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        options.serializeTo(output);
    }


    public static class Handler extends RequestHandler<GetPlayerTimeEntriesRequest, GetPlayerTimeEntriesResponse> {

        private final PlayTimeAPI api;

        public Handler(PlayTimeAPI api) {
            this.api = api;
        }

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public GetPlayerTimeEntriesRequest handleRequest(ByteArrayDataInput input) {
            return new GetPlayerTimeEntriesRequest(LookupTimeListOptions.deserializeFrom(input));
        }

        @Override
        public CompletableFuture<GetPlayerTimeEntriesResponse> processRequest(GetPlayerTimeEntriesRequest request) {
            return api.lookupTimeTops(request.getOptions()).thenApply(GetPlayerTimeEntriesResponse::new);
        }

    }

}

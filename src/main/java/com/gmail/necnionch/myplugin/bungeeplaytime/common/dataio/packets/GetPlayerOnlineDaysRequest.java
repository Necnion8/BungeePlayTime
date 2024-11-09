package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio.ServerMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GetPlayerOnlineDaysRequest extends Request<GetPlayerOnlineDaysResponse> {
    public static final String KEY = "get_player_online_days";
    private final UUID playerId;
    private final LookupTimeOptions options;

    public GetPlayerOnlineDaysRequest(UUID playerId, LookupTimeOptions options) {
        this.playerId = playerId;
        this.options = options;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(playerId.toString());
        options.serializeTo(output);
    }


    public static class Handler extends RequestHandler<GetPlayerOnlineDaysRequest, GetPlayerOnlineDaysResponse> {

        private final PlayTimeAPI api;
        private final ServerMessenger messenger;

        public Handler(PlayTimeAPI api, ServerMessenger messenger) {
            this.api = api;
            this.messenger = messenger;
        }

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public GetPlayerOnlineDaysRequest handleRequest(ByteArrayDataInput input) {
            return new GetPlayerOnlineDaysRequest(UUID.fromString(input.readUTF()), LookupTimeOptions.deserializeFrom(input, messenger.getServerInfo().getName()));
        }

        @Override
        public CompletableFuture<GetPlayerOnlineDaysResponse> processRequest(GetPlayerOnlineDaysRequest request) {
            return api.lookupOnlineDays(request.playerId, request.options)
                    .thenApply(GetPlayerOnlineDaysResponse::new);
        }

    }
}

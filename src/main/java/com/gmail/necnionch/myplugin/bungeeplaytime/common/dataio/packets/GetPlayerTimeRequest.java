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

public class GetPlayerTimeRequest extends Request<GetPlayerTimeResponse> {
    public static final String KEY = "get_player_time";
    private final UUID playerId;
    private final LookupTimeOptions options;

    public GetPlayerTimeRequest(UUID playerId, LookupTimeOptions options) {
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


    public UUID getPlayerId() {
        return playerId;
    }

    public LookupTimeOptions getOptions() {
        return options;
    }


    public static class Handler extends RequestHandler<GetPlayerTimeRequest, GetPlayerTimeResponse> {

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
        public GetPlayerTimeRequest handleRequest(ByteArrayDataInput input) {
            UUID playerId = UUID.fromString(input.readUTF());
            LookupTimeOptions options = LookupTimeOptions.deserializeFrom(input, messenger.getServerInfo().getName());
            return new GetPlayerTimeRequest(playerId, options);
        }

        @Override
        public CompletableFuture<GetPlayerTimeResponse> processRequest(GetPlayerTimeRequest request) {
            return api.lookupTime(request.playerId, request.options)
                    .thenApply(ret -> new GetPlayerTimeResponse(ret.orElse(null)));
        }

    }

}

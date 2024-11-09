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

public class GetPlayerFirstTimeRequest extends Request<GetPlayerFirstTimeResponse> {
    public static final String KEY = "get_player_first_time";
    private final UUID playerId;
    private final LookupTimeOptions options;

    public GetPlayerFirstTimeRequest(UUID playerId, LookupTimeOptions options) {
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


    public static class Handler extends RequestHandler<GetPlayerFirstTimeRequest, GetPlayerFirstTimeResponse> {

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
        public GetPlayerFirstTimeRequest handleRequest(ByteArrayDataInput input) {
            return new GetPlayerFirstTimeRequest(UUID.fromString(input.readUTF()), LookupTimeOptions.deserializeFrom(input, messenger.getServerInfo().getName()));
        }

        @Override
        public CompletableFuture<GetPlayerFirstTimeResponse> processRequest(GetPlayerFirstTimeRequest request) {
            return api.lookupFirstTime(request.playerId, request.options)
                    .thenApply(ret -> new GetPlayerFirstTimeResponse(ret.orElse(0)));
        }

    }
}

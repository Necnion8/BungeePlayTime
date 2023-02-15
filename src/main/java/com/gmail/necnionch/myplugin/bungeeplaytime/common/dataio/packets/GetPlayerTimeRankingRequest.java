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

public class GetPlayerTimeRankingRequest extends Request<GetPlayerTimeRankingResponse> {
    public static final String KEY = "get_player_time_ranking";
    private final UUID playerId;
    private final LookupTimeOptions options;

    public GetPlayerTimeRankingRequest(UUID playerId, LookupTimeOptions options) {
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


    public static class Handler extends RequestHandler<GetPlayerTimeRankingRequest,  GetPlayerTimeRankingResponse> {

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
        public GetPlayerTimeRankingRequest handleRequest(ByteArrayDataInput input) {
            return new GetPlayerTimeRankingRequest(UUID.fromString(input.readUTF()), LookupTimeOptions.deserializeFrom(input, messenger.getServerInfo().getName()));
        }

        @Override
        public CompletableFuture<GetPlayerTimeRankingResponse> processRequest(GetPlayerTimeRankingRequest request) {
            return api.lookupTimeRanking(request.playerId, request.options)
                    .thenApply(ret -> new GetPlayerTimeRankingResponse(ret.orElse(-1)));
        }
    }

}

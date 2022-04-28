package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.RequestHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GetPlayerNameRequest extends Request<GetPlayerNameResponse> {
    public static final String KEY = "get_player_name";
    private final @Nullable UUID searchId;
    private final @Nullable String searchName;

    public GetPlayerNameRequest(@Nullable UUID searchId, @Nullable String searchName) {
        this.searchId = searchId;
        this.searchName = searchName;
    }

    @Override
    public String getDataKey() {
        return KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeUTF(searchId != null ? searchId.toString() : "");
        output.writeUTF(searchName != null ? searchName : "");
    }


    public static class Handler extends RequestHandler<GetPlayerNameRequest, GetPlayerNameResponse> {

        private final PlayTimeAPI api;

        public Handler(PlayTimeAPI api) {
            this.api = api;
        }

        @Override
        public String getDataKey() {
            return KEY;
        }

        @Override
        public GetPlayerNameRequest handleRequest(ByteArrayDataInput input) {
            String searchId = input.readUTF();
            String searchName = input.readUTF();
            return new GetPlayerNameRequest(searchId.isEmpty() ? null : UUID.fromString(searchId), searchName.isEmpty() ? null : searchName);
        }

        @Override
        public CompletableFuture<GetPlayerNameResponse> processRequest(GetPlayerNameRequest request) {
            if (request.searchId != null)
                return api.fetchPlayerName(request.searchId)
                        .thenApply(ret -> new GetPlayerNameResponse(ret.orElse(null)));
            if (request.searchName != null)
                return api.fetchPlayerId(request.searchName)
                        .thenApply(ret -> new GetPlayerNameResponse(ret.orElse(null)));
            return CompletableFuture.completedFuture(new GetPlayerNameResponse(null));
        }
    }

}

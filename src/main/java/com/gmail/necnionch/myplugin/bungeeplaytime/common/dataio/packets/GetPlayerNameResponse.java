package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GetPlayerNameResponse extends Response {

    private final @Nullable PlayerName result;

    public GetPlayerNameResponse(@Nullable PlayerName result) {
        this.result = result;
    }

    public Optional<PlayerName> getResult() {
        return Optional.ofNullable(result);
    }

    @Override
    public String getDataKey() {
        return GetPlayerNameRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        if (result == null) {
            output.writeBoolean(false);
            return;
        }
        output.writeBoolean(true);
        result.serializeTo(output);
    }


    public static class Handler extends ResponseHandler<GetPlayerNameResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerNameRequest.KEY;
        }

        @Override
        public GetPlayerNameResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerNameResponse(input.readBoolean() ? PlayerName.deserializeFrom(input) : null);
        }
    }
}

package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GetPlayerTimeResponse extends Response {

    private final @Nullable PlayerTimeResult result;

    public GetPlayerTimeResponse(@Nullable PlayerTimeResult result) {
        this.result = result;
    }

    @Override
    public String getDataKey() {
        return GetPlayerTimeRequest.KEY;
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

    public Optional<PlayerTimeResult> getResult() {
        return Optional.ofNullable(result);
    }


    public static class Handler extends ResponseHandler<GetPlayerTimeResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerTimeRequest.KEY;
        }

        @Override
        public GetPlayerTimeResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerTimeResponse(input.readBoolean() ? PlayerTimeResult.deserializeFrom(input) : null);
        }

    }

}

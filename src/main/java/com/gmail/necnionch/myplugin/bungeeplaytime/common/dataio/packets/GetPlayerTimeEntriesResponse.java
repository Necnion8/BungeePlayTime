package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class GetPlayerTimeEntriesResponse extends Response {
    private final PlayerTimeEntries result;

    public GetPlayerTimeEntriesResponse(PlayerTimeEntries result) {
        this.result = result;
    }

    @Override
    public String getDataKey() {
        return GetPlayerTimeEntriesRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        result.serializeTo(output);
    }

    public PlayerTimeEntries getResult() {
        return result;
    }


    public static class Handler extends ResponseHandler<GetPlayerTimeEntriesResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerTimeEntriesRequest.KEY;
        }

        @Override
        public GetPlayerTimeEntriesResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerTimeEntriesResponse(PlayerTimeEntries.deserializeFrom(input));
        }

    }

}

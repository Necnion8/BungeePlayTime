package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.OptionalInt;

public class GetPlayerTimeRankingResponse extends Response {

    private final int ranking;

    public GetPlayerTimeRankingResponse(int ranking) {
        this.ranking = ranking;
    }

    public OptionalInt getRanking() {
        return ranking >= 0 ? OptionalInt.of(ranking) : OptionalInt.empty();
    }

    @Override
    public String getDataKey() {
        return GetPlayerTimeRankingRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeInt(ranking);
    }


    public static class Handler extends ResponseHandler<GetPlayerTimeRankingResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerTimeRankingRequest.KEY;
        }

        @Override
        public GetPlayerTimeRankingResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerTimeRankingResponse(input.readInt());
        }
    }

}

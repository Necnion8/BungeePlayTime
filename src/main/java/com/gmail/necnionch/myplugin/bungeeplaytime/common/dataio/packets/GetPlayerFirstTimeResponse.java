package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.OptionalLong;

public class GetPlayerFirstTimeResponse extends Response {

    private final long firstTime;

    public GetPlayerFirstTimeResponse(long firstTime) {
        this.firstTime = firstTime;
    }

    public OptionalLong getFirstTime() {
        return firstTime > 0 ? OptionalLong.of(firstTime) : OptionalLong.empty();
    }

    @Override
    public String getDataKey() {
        return GetPlayerFirstTimeRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeLong(firstTime);
    }


    public static class Handler extends ResponseHandler<GetPlayerFirstTimeResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerFirstTimeRequest.KEY;
        }

        @Override
        public GetPlayerFirstTimeResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerFirstTimeResponse(input.readLong());
        }
    }

}

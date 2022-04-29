package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.OptionalLong;

public class GetPlayerLastTimeResponse extends Response {

    private final long lastTime;

    public GetPlayerLastTimeResponse(long lastTime) {
        this.lastTime = lastTime;
    }

    public OptionalLong getLastTime() {
        return lastTime > 0 ? OptionalLong.of(lastTime) : OptionalLong.empty();
    }

    @Override
    public String getDataKey() {
        return GetPlayerLastTimeRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeLong(lastTime);
    }


    public static class Handler extends ResponseHandler<GetPlayerLastTimeResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerLastTimeRequest.KEY;
        }

        @Override
        public GetPlayerLastTimeResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerLastTimeResponse(input.readLong());
        }
    }

}

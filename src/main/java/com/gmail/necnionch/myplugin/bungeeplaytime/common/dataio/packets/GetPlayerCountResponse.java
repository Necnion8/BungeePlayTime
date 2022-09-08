package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class GetPlayerCountResponse extends Response {

    private final long total;

    public GetPlayerCountResponse(long total) {
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    @Override
    public String getDataKey() {
        return GetPlayerCountRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeLong(total);
    }


    public static class Handler extends ResponseHandler<GetPlayerCountResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerCountRequest.KEY;
        }

        @Override
        public GetPlayerCountResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerCountResponse(input.readLong());
        }
    }

}

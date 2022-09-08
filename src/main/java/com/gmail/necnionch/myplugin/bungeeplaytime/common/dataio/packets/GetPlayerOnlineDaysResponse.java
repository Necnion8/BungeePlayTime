package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.ResponseHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class GetPlayerOnlineDaysResponse extends Response {

    private final long days;

    public GetPlayerOnlineDaysResponse(long days) {
        this.days = days;
    }

    public long getDays() {
        return days;
    }

    @Override
    public String getDataKey() {
        return GetPlayerOnlineDaysRequest.KEY;
    }

    @Override
    public void serialize(ByteArrayDataOutput output) {
        output.writeLong(days);
    }


    public static class Handler extends ResponseHandler<GetPlayerOnlineDaysResponse> {

        @Override
        public String getDataKey() {
            return GetPlayerOnlineDaysRequest.KEY;
        }

        @Override
        public GetPlayerOnlineDaysResponse handleResponse(ByteArrayDataInput input) {
            return new GetPlayerOnlineDaysResponse(input.readLong());
        }
    }

}

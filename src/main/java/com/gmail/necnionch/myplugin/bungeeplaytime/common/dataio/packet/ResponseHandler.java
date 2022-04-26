package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet;

import com.google.common.io.ByteArrayDataInput;

public abstract class ResponseHandler<Res extends Response> {

    public abstract String getDataKey();

    public abstract Res handleResponse(ByteArrayDataInput input);

}

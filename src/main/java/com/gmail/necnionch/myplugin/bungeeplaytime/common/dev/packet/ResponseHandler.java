package com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet;

import com.google.common.io.ByteArrayDataInput;

public abstract class ResponseHandler<R extends Response> {

    public abstract R handleResponse(ByteArrayDataInput input);

}

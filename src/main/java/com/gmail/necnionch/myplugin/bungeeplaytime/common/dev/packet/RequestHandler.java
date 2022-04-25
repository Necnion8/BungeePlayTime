package com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.packet;

import com.google.common.io.ByteArrayDataInput;

public abstract class RequestHandler<R extends Request<Res>, Res extends Response> {

    public abstract R handleRequest(ByteArrayDataInput input);

    public abstract Res processRequest(R request);

}

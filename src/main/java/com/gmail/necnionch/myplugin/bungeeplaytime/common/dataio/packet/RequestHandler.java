package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet;

import com.google.common.io.ByteArrayDataInput;

import java.util.concurrent.CompletableFuture;

public abstract class RequestHandler<R extends Request<Res>, Res extends Response> {
    public abstract String getDataKey();

    public abstract R handleRequest(ByteArrayDataInput input);

    public abstract CompletableFuture<Res> processRequest(R request);

}

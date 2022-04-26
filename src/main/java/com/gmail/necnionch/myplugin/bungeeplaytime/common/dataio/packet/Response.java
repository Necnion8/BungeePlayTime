package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet;

import com.google.common.io.ByteArrayDataOutput;

public abstract class Response {

    public abstract String getDataKey();

    public abstract void serialize(ByteArrayDataOutput output);

}

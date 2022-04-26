package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.DataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import net.md_5.bungee.api.config.ServerInfo;

public class ServerMessenger extends DataMessenger {
    private final String channelName;
    private final BungeeDataMessenger.RequestListener listener;
    private ServerInfo server;

    public ServerMessenger(BungeeDataMessenger parent, ServerInfo server, BungeeDataMessenger.RequestListener listener) {
        super(parent.getLogger(), parent.getExecutor(), parent.getExecutor());
        this.server = server;
        this.channelName = parent.getChannelName();
        this.listener = listener;
    }

    @Override
    protected void writeOut(byte[] data) {
        server.sendData(channelName, data);
    }

    @Override
    protected void writeIn(byte[] data) {
        super.writeIn(data);
    }

    @Override
    protected <Res extends Response> void onRequest(Request<Res> request) {
        listener.onRequest(this, request);
    }


    public ServerInfo getServerInfo() {
        return server;
    }

    public void setServerInfo(ServerInfo server) {
        this.server = server;
    }

}

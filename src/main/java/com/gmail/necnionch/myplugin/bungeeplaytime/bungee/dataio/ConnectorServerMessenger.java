package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks.ConnectorMessenger;
import net.md_5.bungee.api.config.ServerInfo;

public class ConnectorServerMessenger extends ServerMessenger {

    private final ConnectorMessenger connector;
    private boolean detectConnector;

    public ConnectorServerMessenger(BungeeDataMessenger parent, ServerInfo server, BungeeDataMessenger.RequestListener listener, ConnectorMessenger connector) {
        super(parent, server, listener);
        this.connector = connector;
        connector.serverMessengers().put(server.getName(), this);
    }


    @Override
    protected void writeIn(byte[] data) {
        detectConnector = false;
        super.writeIn(data);
    }

    public void writeInFromConnector(byte[] data) {
        detectConnector = true;
        super.writeIn(data);
    }

    @Override
    protected void writeOut(byte[] data) {
        if (detectConnector) {
            connector.send(getServerInfo().getName(), data);
        } else {
            super.writeOut(data);
        }
    }

}

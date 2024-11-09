package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio.ConnectorServerMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.google.common.collect.Maps;
import de.themoep.connectorplugin.connector.ConnectingPlugin;
import de.themoep.connectorplugin.connector.Connector;
import de.themoep.connectorplugin.connector.MessageTarget;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Map;

public class ConnectorMessenger implements ConnectingPlugin {

    private final Plugin plugin;
    private final Connector<?, ?> connector;
    private final Map<String, ConnectorServerMessenger> serverMessengers = Maps.newHashMap();

    public ConnectorMessenger(Plugin plugin, Connector<?, ?> connector) {
        this.plugin = plugin;
        this.connector = connector;
    }

    @Override
    public String getName() {
        return BPTUtil.CONNECTOR_CONNECTING_NAME;
    }

    public Map<String, ConnectorServerMessenger> serverMessengers() {
        return serverMessengers;
    }

    public void register() {
        connector.registerMessageHandler(this, "dataio", (p, m) -> {
            ConnectorServerMessenger s = serverMessengers.get(m.getSendingServer());
            if (s == null) {
                plugin.getLogger().warning("Received from not loaded server : " + m.getSendingServer() + " (by ConnectorMessaging)");
                return;
            }
            s.writeInFromConnector(m.getData());
        });
    }

    public void unregister() {
        connector.unregisterMessageHandlers(this);
    }

    public void send(String serverName, byte[] data) {
        connector.sendData(this, "dataio", MessageTarget.SERVER, serverName, data);
    }

}

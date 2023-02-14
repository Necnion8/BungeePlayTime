package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import de.themoep.connectorplugin.connector.ConnectingPlugin;
import de.themoep.connectorplugin.connector.Connector;
import de.themoep.connectorplugin.connector.MessageTarget;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;

public class ConnectorBukkitDataMessenger extends BukkitDataMessenger implements ConnectingPlugin {

    private final Connector<?, ?> connector;

    public ConnectorBukkitDataMessenger(Executor syncExecutor, Executor asyncExecutor, Plugin owner, String channelName, RequestListener listener, Connector<?, ?> connector) {
        super(syncExecutor, asyncExecutor, owner, channelName, listener);
        this.connector = connector;
    }

    @Override
    public String getName() {
        return BPTUtil.CONNECTOR_CONNECTING_NAME;
    }

    public static ConnectorBukkitDataMessenger register(Plugin owner, String channelName, RequestListener listener, Connector<?, ?> connector) {
        ConnectorBukkitDataMessenger messenger = new ConnectorBukkitDataMessenger(
                (task) -> owner.getServer().getScheduler().runTask(owner, task),
                (task) -> owner.getServer().getScheduler().runTaskAsynchronously(owner, task),
                owner, channelName, listener, connector);
        owner.getServer().getMessenger().registerIncomingPluginChannel(owner, channelName, messenger);
        owner.getServer().getMessenger().registerOutgoingPluginChannel(owner, channelName);

        connector.registerMessageHandler(messenger, "dataio", (o, m) -> {
            messenger.writeIn(m.getData());
        });
        return messenger;
    }

    @Override
    protected void writeOut(byte[] data) {
        connector.sendData(this, "dataio", MessageTarget.PROXY, data);
    }

    public void unregister() {
        try {
            connector.unregisterMessageHandlers(this);
        } finally {
            super.unregister();
        }
    }
}


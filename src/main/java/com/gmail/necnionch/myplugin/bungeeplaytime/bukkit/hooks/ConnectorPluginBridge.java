package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.hooks;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio.BukkitDataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio.ConnectorBukkitDataMessenger;
import de.themoep.connectorplugin.ConnectorPlugin;
import de.themoep.connectorplugin.connector.Connector;
import org.bukkit.plugin.Plugin;

public class ConnectorPluginBridge extends PluginHook {

    private Connector<?, ?> connector;
    private ConnectorBukkitDataMessenger messenger;

    public ConnectorPluginBridge(Plugin owner) {
        super(owner, "ConnectorPlugin");
    }

    @Override
    protected boolean onHook(Plugin plugin) {
        connector = null;
        if (plugin == null || !plugin.isEnabled())
            return false;
        connector = ((ConnectorPlugin<?>) plugin).getConnector();
        return true;
    }

    @Override
    protected void onUnhook() {
        if (messenger != null)
            messenger.unregister();
        messenger = null;
        connector = null;
    }

    @Override
    public boolean isEnabled() {
        return connector != null;
    }


    public ConnectorBukkitDataMessenger registerMessenger(String channelName, BukkitDataMessenger.RequestListener listener) {
        messenger = ConnectorBukkitDataMessenger.register(owner, channelName, listener, connector);
        return messenger;
    }

}

package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.BungeePlayTime;
import de.themoep.connectorplugin.ConnectorPlugin;
import de.themoep.connectorplugin.connector.Connector;
import net.md_5.bungee.api.plugin.Plugin;

public class ConnectorPluginBridge {

    private final BungeePlayTime plugin;
    private Connector<?, ?> connector;
    private ConnectorMessenger messenger;

    public ConnectorPluginBridge(BungeePlayTime plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        connector = null;
        try {
            Plugin tmp = plugin.getProxy().getPluginManager().getPlugin("ConnectorPlugin");
            if (tmp == null)
                return;
            connector = ((ConnectorPlugin<?>) tmp).getConnector();
        } catch (Throwable e) {
            e.printStackTrace();
            connector = null;
        }
    }

    public void unhook() {
        unregisterMessenger();
        messenger = null;
        connector = null;
    }

    public boolean isEnabled() {
        return connector != null;
    }

    public void registerMessenger() {
        messenger = new ConnectorMessenger(plugin, connector);
        messenger.register();
    }

    public void unregisterMessenger() {
        if (messenger != null)
            messenger.unregister();
        messenger = null;
    }

    public ConnectorMessenger getMessenger() {
        return messenger;
    }

}

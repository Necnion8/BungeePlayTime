package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.BukkitConfigDriver;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.CommunicationServiceType;
import org.bukkit.plugin.java.JavaPlugin;

public class MainConfig extends BukkitConfigDriver {
    public MainConfig(JavaPlugin plugin) {
        super(plugin);
    }

    public CommunicationServiceType getCommunicationService() {
        String name = config.getString("communication-service");
        if ("connectorplugin".equalsIgnoreCase(name))
            return CommunicationServiceType.CONNECTOR_PLUGIN;
        return CommunicationServiceType.BUNGEE_MESSAGING;
    }

}

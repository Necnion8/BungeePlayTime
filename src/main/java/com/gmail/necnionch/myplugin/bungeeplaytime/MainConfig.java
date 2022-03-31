package com.gmail.necnionch.myplugin.bungeeplaytime;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.BungeeConfigDriver;
import net.md_5.bungee.api.plugin.Plugin;

public class MainConfig extends BungeeConfigDriver {
    public MainConfig(Plugin plugin) {
        super(plugin);
    }


    public String getAddress() {
        return config.getString("database.mysql.address", "");
    }

    public String getDatabase() {
        return config.getString("database.mysql.database", "");
    }

    public String getUserName() {
        return config.getString("database.mysql.username", "");
    }

    public String getPassword() {
        return config.getString("database.mysql.password", "");
    }



}

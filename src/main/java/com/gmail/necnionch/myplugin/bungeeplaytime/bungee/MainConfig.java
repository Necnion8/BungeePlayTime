package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.BungeeConfigDriver;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, String> getOptions() {
        Configuration section = config.getSection("database.mysql.options");
        if (section != null)
            return section.getKeys().stream()
                    .collect(Collectors.toMap(k -> k, k -> String.valueOf(section.get(k))));
        return Collections.emptyMap();
    }



}

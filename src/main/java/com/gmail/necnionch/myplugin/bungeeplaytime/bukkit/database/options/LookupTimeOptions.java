package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.database.options;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.BungeePlayTime;

public class LookupTimeOptions extends com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions {

    public LookupTimeOptions totalTime(boolean totalTime) {
        super.totalTime(totalTime);
        return this;
    }

    public LookupTimeOptions server(String serverName) {
        super.server(serverName);
        return this;
    }

    public LookupTimeOptions afters(long afters) {
        super.afters(afters);
        return this;
    }

    public LookupTimeOptions currentServer() {
        String serverName = BungeePlayTime.getAPI().getServerNameInBungee();
        if (serverName != null) {
            server(serverName);
        }
        return this;
    }

}

package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.database.options;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.BungeePlayTime;

public class LookupTimeListOptions extends com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions {

    public LookupTimeListOptions count(int count) {
        super.count(count);
        return this;
    }

    public LookupTimeListOptions offset(int offset) {
        super.offset(offset);
        return this;
    }

    @Override
    public LookupTimeListOptions totalTime(boolean totalTime) {
        super.totalTime(totalTime);
        return this;
    }

    @Override
    public LookupTimeListOptions server(String serverName) {
        super.server(serverName);
        return this;
    }

    @Override
    public LookupTimeListOptions afters(long afters) {
        super.afters(afters);
        return this;
    }

    public LookupTimeListOptions currentServer() {
        String serverName = BungeePlayTime.getAPI().getServerNameInBungee();
        if (serverName != null) {
            server(serverName);
        }
        return this;
    }

}

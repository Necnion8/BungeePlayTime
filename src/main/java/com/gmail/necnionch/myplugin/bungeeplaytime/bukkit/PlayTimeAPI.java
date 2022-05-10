package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.IPlayTimeAPI;


public interface PlayTimeAPI extends IPlayTimeAPI {
    boolean isBungeeConnected();
    String getServerNameInBungee();
}

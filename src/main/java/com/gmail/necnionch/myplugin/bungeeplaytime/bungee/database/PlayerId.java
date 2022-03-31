package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import java.util.UUID;

public class PlayerId {

    private final UUID uniqueId;
    private final String name;

    public PlayerId(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

}

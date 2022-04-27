package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result;

import java.util.UUID;

public class PlayerName {

    private final UUID uniqueId;
    private final String name;

    public PlayerName(UUID uniqueId, String name) {
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

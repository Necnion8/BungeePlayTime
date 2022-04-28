package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

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


    public void serializeTo(ByteArrayDataOutput output) {
        output.writeUTF(uniqueId.toString());
        output.writeUTF(name);
    }

    public static PlayerName deserializeFrom(ByteArrayDataInput input) {
        return new PlayerName(UUID.fromString(input.readUTF()), input.readUTF());
    }

}

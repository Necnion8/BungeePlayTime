package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalLong;

public class LookupTimeOptions {

    protected boolean totalTime;
    protected String serverName;
    protected long afters;
    private boolean currentServer;

    public LookupTimeOptions() {}

    public LookupTimeOptions(boolean totalTime, String serverName, long afters) {
        this.totalTime = totalTime;
        this.serverName = serverName;
        this.afters = afters;
    }

    public boolean isTotalTime() {
        return totalTime;
    }

    public Optional<String> getServerName() {
        return Optional.ofNullable(serverName);
    }

    public OptionalLong getAfters() {
        return afters > 0 ? OptionalLong.of(afters) : OptionalLong.empty();
    }


    public LookupTimeOptions totalTime(boolean totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public LookupTimeOptions server(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public LookupTimeOptions afters(long afters) {
        this.afters = afters;
        return this;
    }

    public LookupTimeOptions currentServer() {
        currentServer = true;
        return this;
    }

    public LookupTimeOptions allServer() {
        currentServer = false;
        serverName = null;
        return this;
    }

    public boolean isCurrentServer() {
        return currentServer;
    }

    public void serializeTo(ByteArrayDataOutput output) {
        output.writeBoolean(totalTime);
        output.writeUTF((serverName != null) ? serverName : "");
        output.writeLong(afters);
        output.writeBoolean(currentServer);  // v1.3~
    }

    public static LookupTimeOptions deserializeFrom(ByteArrayDataInput input) {
        boolean totalTime = input.readBoolean();
        String serverName = input.readUTF();
        long afters = input.readLong();
        return new LookupTimeOptions(totalTime, (serverName.isEmpty()) ? null : serverName, afters);
    }

    public static LookupTimeOptions deserializeFrom(ByteArrayDataInput input, @Nullable String senderName) {
        boolean totalTime = input.readBoolean();
        String serverName = input.readUTF();
        long afters = input.readLong();
        boolean currentServer;
        try {
            currentServer = input.readBoolean();  // v1.3~
        } catch (Throwable e) {  // EOF?
            currentServer = false;
        }

        if (serverName.isEmpty())
            serverName = null;

        if (senderName != null && currentServer)
            serverName = senderName;

        return new LookupTimeOptions(totalTime, serverName, afters);
    }

    public LookupTimeOptions copyTo(@Nullable LookupTimeOptions options) {
        if (options == null)
            options = new LookupTimeOptions();
        options.totalTime = totalTime;
        options.serverName = serverName;
        options.afters = afters;
        options.currentServer = currentServer;
        return options;
    }

}

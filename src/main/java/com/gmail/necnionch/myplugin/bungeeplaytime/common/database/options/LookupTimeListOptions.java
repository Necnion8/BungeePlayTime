package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class LookupTimeListOptions extends LookupTimeOptions {

    private int count;
    private int offset;

    public LookupTimeListOptions() {}

    public LookupTimeListOptions(int count, int offset, boolean totalTime, String serverName, long afters) {
        super(totalTime, serverName, afters);
        this.count = count;
        this.offset = offset;
    }

    public OptionalInt getCount() {
        return (count >= 1) ? OptionalInt.of(count) : OptionalInt.empty();
    }

    public OptionalInt getOffset() {
        return (offset >= 1) ? OptionalInt.of(offset) : OptionalInt.empty();
    }


    public LookupTimeListOptions count(int count) {
        this.count = count;
        return this;
    }

    public LookupTimeListOptions offset(int offset) {
        this.offset = offset;
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
        super.currentServer();
        return this;
    }

    public LookupTimeListOptions allServer() {
        super.allServer();
        return this;
    }


    public void serializeTo(ByteArrayDataOutput output) {
        output.writeBoolean(totalTime);
        output.writeUTF((serverName != null) ? serverName : "");
        output.writeLong(afters);
        output.writeInt(count);
        output.writeInt(offset);
        output.writeBoolean(isCurrentServer());  // v1.3~
    }

    public static LookupTimeListOptions deserializeFrom(ByteArrayDataInput input) {
        boolean totalTime = input.readBoolean();
        String serverName = input.readUTF();
        long afters = input.readLong();
        int count = input.readInt();
        int offset = input.readInt();
        return new LookupTimeListOptions(count, offset, totalTime, (serverName.isEmpty()) ? null : serverName, afters);
    }

    public static LookupTimeListOptions deserializeFrom(ByteArrayDataInput input, @Nullable String senderName) {
        boolean totalTime = input.readBoolean();
        String serverName = input.readUTF();
        long afters = input.readLong();
        int count = input.readInt();
        int offset = input.readInt();
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

        return new LookupTimeListOptions(count, offset, totalTime, serverName, afters);
    }

    public LookupTimeListOptions copyTo(@Nullable LookupTimeListOptions options) {
        if (options == null)
            options = new LookupTimeListOptions();
        super.copyTo(options);
        options.count = count;
        options.offset = offset;
        return options;
    }

}

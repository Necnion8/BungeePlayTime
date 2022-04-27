package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options;

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

}

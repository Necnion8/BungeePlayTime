package com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options;

import java.util.Optional;
import java.util.OptionalLong;

public class LookupTimeOptions {

    private boolean totalTime;
    private String serverName;
    private long afters;

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

}

package com.gmail.necnionch.myplugin.bungeeplaytime.common;


public enum AFKState {
    FALSE(0),
    TRUE(1),
    UNKNOWN(-1);

    private final int value;

    AFKState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AFKState valueOf(int value) {
        switch (value) {
            case 0:
                return FALSE;
            case 1:
                return TRUE;
            case -1:
                return UNKNOWN;
            default:
                throw new IllegalArgumentException("unknown state: " + value);
        }
    }

    public static AFKState valueOrNoneOf(int value) {
        switch (value) {
            case 0:
                return FALSE;
            case 1:
                return TRUE;
            default:
                return UNKNOWN;
        }
    }

    public boolean isPlayed() {
        if (UNKNOWN.equals(this))
            return isPlayedInUnknownState();
        return !TRUE.equals(this);
    }

    public static boolean isPlayedInUnknownState() {
        return BPTUtil.isPlayedInUnknownState();
    }

}

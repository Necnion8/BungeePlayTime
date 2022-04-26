package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;


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
        if (TRUE.equals(this))
            return true;
        if (FALSE.equals(this))
            return false;
        return isPlayedInUnknownState();
    }

    public static boolean isPlayedInUnknownState() {
        BungeePlayTime instance = BungeePlayTime.getInstance();
        if (instance != null) {
            return instance.isPlayedInUnknownState();
        }
        return false;
    }

}

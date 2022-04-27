package com.gmail.necnionch.myplugin.bungeeplaytime.common;

import net.md_5.bungee.api.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BPTUtil {
    public static final String MESSAGE_CHANNEL_AFK_STATE = "bptime:afkstate";
    public static final String MESSAGE_CHANNEL_DATA = "bptime:data";
    private static boolean playedInUnknownState;

    public static String formatEpochTime(long epoch) {
        return new SimpleDateFormat("yyyy年 M月 d日").format(new Date(epoch));
    }

    public static String formatTimeText(long millis) {
        long offset = millis / 1000L;
        int hours = (int) offset / 3600;
        offset -= (hours * 3600);
        int minutes = (int) offset / 60;
        return ((hours > 0) ? ChatColor.GOLD.toString() + hours + ChatColor.GRAY + "時間 " : "")
                + ChatColor.GOLD + minutes + ChatColor.GRAY + "分";
    }

    public static boolean isPlayedInUnknownState() {
        return playedInUnknownState;
    }

    public static void setPlayedInUnknownState(boolean played) {
        playedInUnknownState = played;
    }


}

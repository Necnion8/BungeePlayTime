package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.timer;

import java.util.concurrent.TimeUnit;


public interface Timer {
    ScheduledTask schedule(Runnable runnable, long delay);

    static Timer createOfBukkit(org.bukkit.plugin.Plugin plugin) {
        return (runnable, delay) -> {
            org.bukkit.scheduler.BukkitTask bukkitTask = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay / 50);
            return new ScheduledTask.Bukkit(bukkitTask);
        };
    }

    static Timer createOfBungee(net.md_5.bungee.api.plugin.Plugin plugin) {
        return (runnable, delay) -> {
            net.md_5.bungee.api.scheduler.ScheduledTask bungeeTask = plugin.getProxy().getScheduler().schedule(plugin, runnable, delay, TimeUnit.MILLISECONDS);
            return new ScheduledTask.Bungee(bungeeTask);
        };
    }

}

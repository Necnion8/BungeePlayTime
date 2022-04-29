package com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.timer;

import org.bukkit.scheduler.BukkitTask;

public interface ScheduledTask {
    void cancel();

    class Bukkit implements ScheduledTask {
        private final BukkitTask task;

        public Bukkit(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

    }

    class Bungee implements ScheduledTask {
        private final net.md_5.bungee.api.scheduler.ScheduledTask task;

        public Bungee(net.md_5.bungee.api.scheduler.ScheduledTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

    }


}

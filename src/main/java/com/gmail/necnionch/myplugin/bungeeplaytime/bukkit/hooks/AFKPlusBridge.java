package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.hooks;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.DataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChange;
import com.google.common.collect.Maps;
import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class AFKPlusBridge extends PluginHook implements Listener {
    private AFKPlus afkPlus;
    private BukkitTask task;
    private final Map<UUID, Long> afkTimeout = Maps.newHashMap();
    private long lastExecutingAFKCommand;


    public AFKPlusBridge(BungeePlayTime owner) {
        super(owner, "AFKPlus");
    }

    @Override
    protected boolean onHook(Plugin plugin) {
        afkTimeout.clear();
        try {
            Class.forName("net.lapismc.afkplus.AFKPlus");
            if (plugin instanceof AFKPlus && plugin.isEnabled()) {
                afkPlus = ((AFKPlus) plugin);
                owner.getServer().getPluginManager().registerEvents(this, owner);
                task = owner.getServer().getScheduler().runTaskTimer(owner, this::timer, 0, 20);
                return true;
            }
        } catch (ClassNotFoundException ignored) {}
        return false;
    }

    @Override
    protected void onUnhook() {
        HandlerList.unregisterAll(this);
        if (task != null && !task.isCancelled())
            task.cancel();
        afkTimeout.clear();
    }

    private DataMessenger getMessenger() {
        return ((BungeePlayTime) owner).getMessenger();
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStart(AFKStartEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        // by command? hacky
        if (System.currentTimeMillis() - lastExecutingAFKCommand <= 2) {
            lastExecutingAFKCommand = 0;
            getMessenger().send(new AFKChange(uuid, AFKState.TRUE));
        } else {
            afkTimeout.put(uuid, System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStop(AFKStopEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        Long afkTime = afkTimeout.remove(uuid);
        if (afkTime == null) {
            getMessenger().send(new AFKChange(uuid, AFKState.FALSE));
        }
    }

    @EventHandler
    public void onCommand(ServerCommandEvent event) {
        if (event.getCommand().startsWith("afk") || event.getCommand().startsWith("afk:afk")) {
            lastExecutingAFKCommand = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/afk") || event.getMessage().startsWith("/afk:afk")) {
            lastExecutingAFKCommand = System.currentTimeMillis();
        }
    }


    public boolean isAFK(OfflinePlayer player) {
        if (afkTimeout.containsKey(player.getUniqueId()))
            return false;
        return afkPlus.getPlayer(player).isAFK();
    }

    public void setAFK(UUID playerId, boolean afk) {
        AFKPlusPlayer afkPlayer = afkPlus.getPlayer(playerId);
        if (afkPlayer != null) {
            if (afk) {
                if (!afkPlayer.isAFK()) {
                    afkPlayer.startAFK();
                } else {
                    afkPlayer.forceStartAFK();
                }
            } else {
                if (afkPlayer.isAFK()) {
                    afkPlayer.stopAFK();
                } else {
                    afkPlayer.forceStopAFK();
                }
            }
        }
    }


    private void timer() {
        BungeePlayTime owner = (BungeePlayTime) this.owner;
        for (Iterator<Map.Entry<UUID, Long>> it = afkTimeout.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Long> e = it.next();
            if (System.currentTimeMillis() - e.getValue() > (owner.getAFKMinutes() * 60 * 1000L)) {
                getMessenger().send(new AFKChange(e.getKey(), AFKState.TRUE, e.getValue()));
                it.remove();
            }
        }

    }

}

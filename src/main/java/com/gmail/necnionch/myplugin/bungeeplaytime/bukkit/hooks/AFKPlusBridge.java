package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.hooks;

import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.BungeePlayTime;
import com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio.BukkitDataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChange;
import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class AFKPlusBridge extends PluginHook implements Listener {

    private AFKPlus afkPlus;

    public AFKPlusBridge(BungeePlayTime owner) {
        super(owner, "AFKPlus");
    }

    @Override
    protected boolean onHook(Plugin plugin) {
        try {
            Class.forName("net.lapismc.afkplus.AFKPlus");
            if (plugin instanceof AFKPlus && plugin.isEnabled()) {
                afkPlus = ((AFKPlus) plugin);
                owner.getServer().getPluginManager().registerEvents(this, owner);
                return true;
            }
        } catch (ClassNotFoundException ignored) {}
        return false;
    }

    @Override
    protected void onUnhook() {
        HandlerList.unregisterAll(this);
    }

    private BukkitDataMessenger getMessenger() {
        return ((BungeePlayTime) owner).getMessenger();
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStart(AFKStartEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        getMessenger().send(AFKChange.fromBukkit(uuid, AFKState.TRUE));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStop(AFKStopEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        getMessenger().send(AFKChange.fromBukkit(uuid, AFKState.FALSE));
    }


    public boolean isAFK(OfflinePlayer player) {
        return afkPlus.getPlayer(player).isAFK();
    }

}

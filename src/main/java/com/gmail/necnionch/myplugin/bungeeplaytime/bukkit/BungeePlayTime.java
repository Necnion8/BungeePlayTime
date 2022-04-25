package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.Test;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example.ItemRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example.ItemResponse;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example.PingRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.example.PingResponse;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;


public class BungeePlayTime extends JavaPlugin implements Listener {

    private Test.BukkitDataIO dataSender;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, BPTUtil.MESSAGE_CHANNEL_AFK_STATE);

        dataSender = new Test.BukkitDataIO(this, "bptime:data");
        dataSender.registerHandler("ping", new PingRequest.PingRequestHandler());
        dataSender.registerHandler("ping", new PingResponse.PingResponseHandler());
        dataSender.registerHandler("item", new ItemResponse.Handler());
        getServer().getMessenger().registerIncomingPluginChannel(this, "bptime:data", dataSender);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "bptime:data");

        getCommand("testtestsend").setExecutor((sender, command, label, args) -> {
//            dataSender.send(new PingRequest((args.length >= 1) ? String.join(" ", args) : "empty")).whenComplete((ret, err) -> {
//                if (err != null) {
//                    sender.sendMessage(ChatColor.RED + "err: " + err.getClass().getSimpleName());
//                } else {
//                    sender.sendMessage("res: " + ret.getResponseMessage());
//                }
//            });
            ItemStack itemStack = ((Player) sender).getInventory().getItemInMainHand();
            String material = itemStack.getType().name();
            int amount = itemStack.getAmount();
            dataSender.send(new ItemRequest(material, amount)).whenComplete((ret, err) -> {
                if (err != null) {
                    sender.sendMessage(ChatColor.RED + "err: " + err.getClass().getSimpleName());
                } else  {
                    sender.sendMessage("extra : " + ret.getExtra());
                }
            });
            return true;
        });
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStart(AFKStartEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        AFKState state = new AFKState(uuid, true);
        player.sendPluginMessage(this, BPTUtil.MESSAGE_CHANNEL_AFK_STATE, state.serialize());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAFKStop(AFKStopEvent event) {
        UUID uuid = event.getPlayer().getUUID();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        AFKState state = new AFKState(uuid, false);
        player.sendPluginMessage(this, BPTUtil.MESSAGE_CHANNEL_AFK_STATE, state.serialize());
    }



    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            dataSender.send(new PingRequest("hi join")).whenComplete((ret, err) -> {
                if (err != null) {
                    getLogger().info("returned error : " + err.getClass().getSimpleName());
                } else {
                    getLogger().info("returned : " + ret.getResponseMessage());
                }
            });
        }, 2);

    }

}

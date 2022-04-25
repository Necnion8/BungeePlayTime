package com.gmail.necnionch.myplugin.bungeeplaytime.common;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dev.DataIO;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Test {

    public Test() {
//
//
//        Executor syncExecutor = (task) -> ProxyServer.getInstance().getScheduler().runAsync(null, task);
//        Executor asyncExecutor = (task) -> ProxyServer.getInstance().getScheduler().runAsync(null, task);
//
//        ServerInfo server = ProxyServer.getInstance().getServerInfo("test");
//        BungeeDataSender bungeeDataSender = new BungeeDataSender(server, syncExecutor, asyncExecutor);
//
//        Plugin plugin = null;
//        syncExecutor = (task) -> Bukkit.getServer().getScheduler().runTask(plugin, task);
//        asyncExecutor = (task) -> Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, task);
//
//        BukkitDataSender bukkitDataSender = new BukkitDataSender(plugin, syncExecutor, asyncExecutor);
//
//
//        bukkitDataSender.send(new PingRequest("Hi")).whenComplete((res, err) -> {
//            if (res != null) {
//                res.getResponseMessage();
//            }
//
//        });
//

    }



    public static class BungeeDataIO extends DataIO implements Listener {
        private final ServerInfo client;
        private final String channelName;

        public BungeeDataIO(net.md_5.bungee.api.plugin.Plugin owner, ServerInfo client, String channelName) {
            super(
                    owner.getLogger(),
                    (task) -> ProxyServer.getInstance().getScheduler().runAsync(owner, task),
                    (task) -> ProxyServer.getInstance().getScheduler().runAsync(owner, task)
            );
            this.client = client;
            this.channelName = channelName;
        }


        @Override
        protected void writeOut(byte[] data) {
            client.sendData(channelName, data);
        }

        @EventHandler
        public void onPluginMessage(PluginMessageEvent event) {
            if (channelName.equalsIgnoreCase(event.getTag()) && event.getSender() instanceof Server) {
                if (client.equals(((Server) event.getSender()).getInfo())) {
                    writeIn(event.getData());
                }
            }
        }

    }

    public static class BukkitDataIO extends DataIO implements PluginMessageListener {
        private final Plugin owner;
        private final String channelName;

        public BukkitDataIO(Plugin owner, String channelName) {
            super(
                    owner.getLogger(),
                    (task) -> owner.getServer().getScheduler().runTask(owner, task),
                    (task) -> owner.getServer().getScheduler().runTaskAsynchronously(owner, task)
            );
            this.owner = owner;
            this.channelName = channelName;
        }

        @Override
        protected void writeOut(byte[] data) {
            Bukkit.getServer().sendPluginMessage(owner, channelName, data);
        }

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] message) {
            if (channelName.equalsIgnoreCase(channel))
                writeIn(message);
        }

    }

}

package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.DataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChange;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.PingRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.PingResponse;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.SettingChangeResponse;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BungeeDataMessenger implements Listener {
    private final Plugin plugin;
    private final String channelName;
    private final Executor executor;

    private final Map<String, ServerMessenger> messengers = Maps.newConcurrentMap();
    private final Set<ServerMessenger> actives = Sets.newConcurrentHashSet();
    private final Set<DataMessenger.Requested<PingResponse>> pingWaits = Sets.newConcurrentHashSet();
    private final RequestListener listener;

    public BungeeDataMessenger(Plugin plugin, String channelName, RequestListener listener) {
        this.plugin = plugin;
        this.channelName = channelName;
        this.listener = listener;
        this.executor = (task) -> ProxyServer.getInstance().getScheduler().runAsync(plugin, task);
    }

    public static BungeeDataMessenger register(Plugin plugin, String channelName, RequestListener listener) {
        BungeeDataMessenger messenger = new BungeeDataMessenger(plugin, channelName, listener);
        plugin.getProxy().getPluginManager().registerListener(plugin, messenger);
        plugin.getProxy().registerChannel(channelName);
        return messenger;
    }

    public void unregister() {
        plugin.getProxy().getPluginManager().unregisterListener(this);
        plugin.getProxy().unregisterChannel(channelName);
    }


    public void updateServerMessengers() {
        cancelPingAll();

        Map<String, ServerInfo> servers = Maps.newHashMap(plugin.getProxy().getServers());
        Map<String, ServerMessenger> messengers = Maps.newHashMap(this.messengers);

        Set<ServerMessenger> removed = Sets.newHashSet();
        messengers.forEach((name, messenger) -> {
            ServerInfo newInfo = servers.remove(name);
            if (newInfo == null) {
                // remove in olds
                removed.add(messenger);
            } else {
                // update info
                messenger.setServerInfo(newInfo);
            }
        });
        messengers.values().removeAll(removed);
        actives.removeAll(removed);

        this.messengers.clear();
        this.messengers.putAll(messengers);
        // add to new info
        servers.forEach((name, info) -> createMessenger(info));

        // refresh active list
        sendPingAll();
    }

    public <R extends Request<Res>, Res extends Response> DataMessenger.Requested<Res> send(ServerInfo server, R request) {
        ServerMessenger messenger = getMessenger(server);
        if (messenger == null)
            throw new IllegalArgumentException("not loaded server: " + server.getName());

        return messenger.send(request);
    }

    @EventHandler
    public void onReload(ProxyReloadEvent event) {
        updateServerMessengers();
    }

    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if (!channelName.equalsIgnoreCase(event.getTag()) || !(event.getSender() instanceof Server))
            return;

        Server server = (Server) event.getSender();

        ServerMessenger messenger = getMessenger(server.getInfo());
        if (messenger == null) {
            plugin.getLogger().warning("Received from not loaded server : " + server.getInfo().getName());
            return;
        }

        messenger.writeIn(event.getData());
    }


    public ServerMessenger getMessenger(ServerInfo serverInfo) {
        if (messengers.containsKey(serverInfo.getName())) {
            return messengers.get(serverInfo.getName());
        }
        return null;
    }

    private ServerMessenger createMessenger(ServerInfo serverInfo) {
        ServerMessenger messenger = new ServerMessenger(this, serverInfo, listener);
        messenger.registerHandler(new PingRequest.Handler());
        messenger.registerHandler(new PingResponse.Handler());
        messenger.registerHandler(new AFKChange.Handler());
        messenger.registerHandler(new SettingChangeResponse.Handler());
        messengers.put(serverInfo.getName(), messenger);
        return messenger;
    }

    private TaskScheduler getScheduler() {
        return plugin.getProxy().getScheduler();
    }


    public void removeActive(ServerInfo serverInfo) {
        actives.removeIf(m -> m.getServerInfo().equals(serverInfo));
    }

    public void addActive(ServerMessenger messenger) {
        if (!messengers.containsValue(messenger))
            throw new IllegalArgumentException("not contains messengers");
        onActiveMessenger(messenger);
    }

    public Set<ServerMessenger> actives() {
        return Collections.unmodifiableSet(actives);
    }

    public void sendPingAll() {
        actives.clear();
        plugin.getProxy().getServers().values().stream()
                .filter(s -> !s.getPlayers().isEmpty())
                .map(info -> messengers.get(info.getName()))
                .filter(Objects::nonNull)
                .forEach(this::sendPing);
    }

    public void sendPing(ServerInfo serverInfo) {
        ServerMessenger messenger = getMessenger(serverInfo);
        if (messenger != null) {
            getScheduler().schedule(plugin, () -> sendPing(messenger), 50, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelPingAll() {
        pingWaits.forEach(DataMessenger.Requested::cancel);
        pingWaits.clear();
    }

    private void sendPing(ServerMessenger messenger) {
        actives.remove(messenger);
        DataMessenger.Requested<PingResponse> request = messenger.send(new PingRequest());
        pingWaits.add(request);

        request.whenComplete((ret, err) -> {
            if (ret != null)
                onActiveMessenger(messenger);

            pingWaits.remove(request);
        });

        getScheduler().schedule(plugin, () -> {  // timeout
            pingWaits.remove(request);
            request.cancel();
        }, 3, TimeUnit.SECONDS);
    }

    public String getChannelName() {
        return channelName;
    }

    public Executor getExecutor() {
        return executor;
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }


    private void onActiveMessenger(ServerMessenger messenger) {
        actives.add(messenger);
        listener.onActiveMessenger(messenger);
    }


    public interface RequestListener {
        <Res extends Response> void onRequest(ServerMessenger messenger, Request<Res> request);
        void onActiveMessenger(ServerMessenger messenger);
    }

}

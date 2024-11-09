package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.dataio;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.PlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.hooks.ConnectorPluginBridge;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.DataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.*;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.timer.Timer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BungeeDataMessenger implements Listener {
    private final Plugin plugin;
    private final String channelName;
    private final Executor executor;

    private final Map<String, ServerMessenger> messengers = Maps.newConcurrentMap();
    private final Set<ServerMessenger> actives = Sets.newConcurrentHashSet();
    private final RequestListener listener;
    private final Timer timer;
    private final ConnectorPluginBridge connectorBridge;

    public BungeeDataMessenger(Plugin plugin, String channelName, RequestListener listener, ConnectorPluginBridge connectorBridge) {
        this.plugin = plugin;
        this.channelName = channelName;
        this.listener = listener;
        this.executor = (task) -> ProxyServer.getInstance().getScheduler().runAsync(plugin, task);
        this.timer = Timer.createOfBungee(plugin);
        this.connectorBridge = connectorBridge;
    }

    public static BungeeDataMessenger register(Plugin plugin, String channelName, RequestListener listener, ConnectorPluginBridge connectorBridge) {
        BungeeDataMessenger messenger = new BungeeDataMessenger(plugin, channelName, listener, connectorBridge);
        plugin.getProxy().getPluginManager().registerListener(plugin, messenger);
        plugin.getProxy().registerChannel(channelName);
        return messenger;
    }

    public void unregister() {
        plugin.getProxy().getPluginManager().unregisterListener(this);
        plugin.getProxy().unregisterChannel(channelName);
        actives.forEach(DataMessenger::cleanup);
    }


    public void updateServerMessengers() {
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
        actives.forEach(DataMessenger::cleanup);
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

    private void createMessenger(ServerInfo serverInfo) {
        ServerMessenger messenger;
        if (connectorBridge.isEnabled()) {
            messenger = new ConnectorServerMessenger(this, serverInfo, listener, connectorBridge.getMessenger());
        } else {
            messenger = new ServerMessenger(this, serverInfo, listener);
        }

        messenger.registerHandler(new PingRequest.Handler());
        messenger.registerHandler(new PingResponse.Handler());
        messenger.registerHandler(new AFKChange.Handler());
        messenger.registerHandler(new AFKChangeResponse.Handler());
        messenger.registerHandler(new SettingChangeResponse.Handler());

        PlayTimeAPI api = ((PlayTimeAPI) plugin);
        messenger.registerHandler(new GetPlayerTimeRequest.Handler(api, messenger));
        messenger.registerHandler(new GetPlayerTimeEntriesRequest.Handler(api, messenger));
        messenger.registerHandler(new GetPlayerTimeRankingRequest.Handler(api, messenger));
        messenger.registerHandler(new GetPlayerFirstTimeRequest.Handler(api, messenger));
        messenger.registerHandler(new GetPlayerLastTimeRequest.Handler(api, messenger));
        messenger.registerHandler(new GetPlayerCountRequest.Handler(api, messenger));
        messenger.registerHandler(new GetPlayerOnlineDaysRequest.Handler(api, messenger));
        messenger.registerHandler(new GetPlayerNameRequest.Handler(api));

        messengers.put(serverInfo.getName(), messenger);
    }

    public Timer getTimer() {
        return timer;
    }

    public void removeActive(ServerInfo serverInfo) {
        for (Iterator<ServerMessenger> it = actives.iterator(); it.hasNext(); ) {
            ServerMessenger messenger = it.next();
            if (messenger.getServerInfo().equals(serverInfo)) {
                messenger.cleanup();
                it.remove();
            }
        }
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
        sendPing(messenger);
    }

    private void sendPing(ServerMessenger messenger) {
        actives.remove(messenger);
         messenger.send(new PingRequest(), 1000).whenComplete((ret, err) -> {
            if (ret != null)
                onActiveMessenger(messenger);
        });
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

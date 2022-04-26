package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit.dataio;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.DataMessenger;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Request;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packet.Response;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.AFKChangeResponse;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.PingRequest;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.PingResponse;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.dataio.packets.SettingChange;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;


public class BukkitDataMessenger extends DataMessenger implements PluginMessageListener {
    private final Plugin owner;
    private final String channelName;
    private final RequestListener listener;

    public BukkitDataMessenger(Executor syncExecutor, Executor asyncExecutor, Plugin owner, String channelName, RequestListener listener) {
        super(owner.getLogger(), syncExecutor, asyncExecutor);
        this.owner = owner;
        this.channelName = channelName;
        this.listener = listener;

        registerHandler(new PingRequest.Handler());
        registerHandler(new PingResponse.Handler());
        registerHandler(new AFKChangeResponse.Handler());
        registerHandler(new SettingChange.Handler());
    }

    public static BukkitDataMessenger register(Plugin owner, String channelName, RequestListener listener) {
        BukkitDataMessenger messenger = new BukkitDataMessenger(
                (task) -> owner.getServer().getScheduler().runTask(owner, task),
                (task) -> owner.getServer().getScheduler().runTaskAsynchronously(owner, task),
                owner, channelName, listener);
        owner.getServer().getMessenger().registerIncomingPluginChannel(owner, channelName, messenger);
        owner.getServer().getMessenger().registerOutgoingPluginChannel(owner, channelName);
        return messenger;
    }

    public void unregister() {
        owner.getServer().getMessenger().unregisterIncomingPluginChannel(owner, channelName, this);
        owner.getServer().getMessenger().unregisterOutgoingPluginChannel(owner, channelName);
    }


    @Override
    protected void writeOut(byte[] data) {
        Bukkit.getServer().sendPluginMessage(owner, channelName, data);
    }

    @Override
    protected <Res extends Response> void onRequest(Request<Res> processedResponse) {
        listener.onRequest(processedResponse);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (channelName.equalsIgnoreCase(channel))
            writeIn(message);
    }


    public interface RequestListener {
        <Res extends Response> void onRequest(Request<Res> request);
    }


}
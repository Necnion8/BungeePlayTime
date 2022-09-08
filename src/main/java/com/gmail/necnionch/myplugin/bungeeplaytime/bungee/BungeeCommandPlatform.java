package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.CommandPlatform;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandBungee;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BungeeCommandPlatform implements CommandPlatform {
    @Override
    public Optional<CommandSender> getPlayer(String name) {
        return Optional.ofNullable(ProxyServer.getInstance().getPlayer(name))
                .map(CommandBungee::conv);
    }

    @Override
    public List<CommandSender> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers()
                .stream()
                .map(CommandBungee::conv)
                .collect(Collectors.toList());
    }

}

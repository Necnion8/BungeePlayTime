package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.CommandPlatform;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandBukkit;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BukkitCommandPlatform implements CommandPlatform {

    @Override
    public Optional<CommandSender> getPlayer(String name) {
        return Optional.ofNullable(Bukkit.getPlayer(name))
                .map(CommandBukkit::conv);
    }

    @Override
    public List<CommandSender> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(CommandBukkit::conv)
                .collect(Collectors.toList());
    }

}

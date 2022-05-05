package com.gmail.necnionch.myplugin.bungeeplaytime.common;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.command.CommandSender;

import java.util.List;
import java.util.Optional;

public interface CommandPlatform {
    Optional<CommandSender> getPlayer(String name);
    List<CommandSender> getOnlinePlayers();
}

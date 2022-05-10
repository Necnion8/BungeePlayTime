package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.IPlayTimeAPI;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;


public interface PlayTimeAPI extends IPlayTimeAPI {
    Collection<PlayerTime> getPlayers();
    Optional<PlayerTime> getPlayer(UUID playerId);
}

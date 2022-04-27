package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeResult;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.LookupTimeOptions;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public interface PlayTimeAPI {
    Collection<PlayerTime> getPlayers();

    Optional<PlayerTime> getPlayer(UUID playerId);


    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, LookupTimeOptions options);

    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId);

    CompletableFuture<PlayerTimeEntries> lookupTimeTops(LookupTimeListOptions options);

    CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, LookupTimeOptions options);

    CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId);

    CompletableFuture<OptionalLong> lookupLastTime(UUID playerId);


    CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId);

    CompletableFuture<Optional<PlayerName>> fetchPlayerId(String playerName);

}

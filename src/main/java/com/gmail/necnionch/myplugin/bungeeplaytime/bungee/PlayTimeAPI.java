package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public interface PlayTimeAPI {
    Collection<PlayerTime> getPlayers();

    Optional<PlayerTime> getPlayer(UUID playerId);


    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, LookupTimeOptions options);

    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId);

    CompletableFuture<PlayerTimeEntries> lookupTimeTops(LookupTimeListOptions options);

    CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, LookupTimeOptions options);

    CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId, LookupTimeOptions options);

    CompletableFuture<OptionalLong> lookupLastTime(UUID playerId, LookupTimeOptions options);


    CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId);

    CompletableFuture<Optional<PlayerName>> fetchPlayerId(String playerName);

}

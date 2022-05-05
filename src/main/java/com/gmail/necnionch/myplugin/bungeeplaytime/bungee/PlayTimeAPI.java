package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.IPlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public interface PlayTimeAPI extends IPlayTimeAPI {
    Collection<PlayerTime> getPlayers();

    Optional<PlayerTime> getPlayer(UUID playerId);


    @Override
    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, LookupTimeOptions options);

    @Override
    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId);

    @Override
    CompletableFuture<PlayerTimeEntries> lookupTimeTops(LookupTimeListOptions options);

    @Override
    CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, LookupTimeOptions options);

    @Override
    CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId);

    @Override
    CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId, LookupTimeOptions options);

    @Override
    CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId);

    @Override
    CompletableFuture<OptionalLong> lookupLastTime(UUID playerId, LookupTimeOptions options);

    @Override
    CompletableFuture<OptionalLong> lookupLastTime(UUID playerId);


    @Override
    CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId);

    @Override
    CompletableFuture<Optional<PlayerName>> fetchPlayerId(String playerName);

}

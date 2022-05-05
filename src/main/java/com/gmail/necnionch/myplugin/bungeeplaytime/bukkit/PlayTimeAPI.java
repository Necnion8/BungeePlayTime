package com.gmail.necnionch.myplugin.bungeeplaytime.bukkit;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.IPlayTimeAPI;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface PlayTimeAPI extends IPlayTimeAPI {

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


    boolean isBungeeConnected();

    String getServerNameInBungee();

}

package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public interface PlayTimeAPI {
    Collection<PlayerTime> getPlayers();

    Optional<PlayerTime> getPlayer(UUID playerId);


    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId, long afters);

    CompletableFuture<Optional<PlayerTimeResult>> lookupTime(UUID playerId);

    CompletableFuture<PlayerTimeEntries> lookupTimeTops(int count, int offset, boolean totalTime, long afters);

    CompletableFuture<PlayerTimeEntries> lookupTimeTops(int count, int offset, boolean totalTime);

    CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, boolean totalTime, long afters);

    CompletableFuture<OptionalInt> lookupTimeRanking(UUID playerId, boolean totalTime);

    CompletableFuture<OptionalLong> lookupFirstTime(UUID playerId);

    CompletableFuture<OptionalLong> lookupLastTime(UUID playerId);


    CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId);

    CompletableFuture<Optional<PlayerName>> fetchPlayerId(String playerName);


    String formatTimeText(long millis);

}

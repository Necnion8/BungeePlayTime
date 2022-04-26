package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeResult;

import java.sql.SQLException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

public interface Database {
    /*
      table 'users'
        uuid        VARCHAR(36) UNIQUE
        name        VARCHAR
      table 'times'
        uuid        VARCHAR(36) UNIQUE
        startTime    BIGINT
        time        BIGINT
        server      ntext
        isAFK       int
     */

    boolean openConnection() throws SQLException;

    boolean isClosed();

    void close() throws Exception;

    void init() throws SQLException;

    void putTime(UUID playerId, String playerName, long startTime, long time, String server, AFKState afk) throws SQLException;


    Optional<PlayerTimeResult> lookupTime(UUID playerId, long afters) throws SQLException;

    PlayerTimeEntries lookupTimeTops(int count, int offset, boolean totalTime, long afters) throws SQLException;

    OptionalInt lookupTimeRanking(UUID playerId, boolean totalTime, long afters) throws SQLException;

    OptionalLong lookupFirstTime(UUID playerId) throws SQLException;

    OptionalLong lookupLastTime(UUID playerId) throws SQLException;


    Optional<PlayerName> getPlayerName(UUID playerId) throws SQLException;

    Optional<PlayerName> getPlayerId(String playerName) throws SQLException;

}

package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeListOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.options.LookupTimeOptions;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.database.result.PlayerTimeResult;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

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


    Optional<PlayerTimeResult> lookupTime(UUID playerId, LookupTimeOptions options) throws SQLException;

    PlayerTimeEntries lookupTimeTops(LookupTimeListOptions options) throws SQLException;

    OptionalInt lookupTimeRanking(UUID playerId, LookupTimeOptions options) throws SQLException;

    OptionalLong lookupFirstTime(UUID playerId, LookupTimeOptions options) throws SQLException;

    OptionalLong lookupLastTime(UUID playerId, LookupTimeOptions options) throws SQLException;

    long lookupPlayerCount(LookupTimeOptions options) throws SQLException;

    long lookupOnlineDays(UUID playerId, LookupTimeOptions options) throws SQLException;


    Optional<PlayerName> getPlayerName(UUID playerId) throws SQLException;

    Optional<PlayerName> getPlayerId(String playerName) throws SQLException;


    default @Nullable Map<UUID, String> cachedPlayerNames() {
        return null;
    }

}

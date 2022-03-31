package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import java.sql.SQLException;
import java.util.Optional;
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

    void putTime(UUID playerId, String playerName, long startTime, long time, String server, boolean isAFK) throws SQLException;


    Optional<LookupPlayerResult> lookupTime(UUID playerId) throws SQLException;

    Optional<LookupPlayerResult> lookupTime(UUID playerId, long afters) throws SQLException;

    LookupTop lookupTimeTops(int lookupCount, boolean afks) throws SQLException;

    LookupTop lookupTimeTops(int lookupCount, boolean afks, long afters) throws SQLException;


    Optional<PlayerId> getPlayerName(UUID playerId) throws SQLException;

    Optional<PlayerId> getPlayerId(String playerName) throws SQLException;

}

package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public interface Database {
    /*
      table 'users'
        uuid        VARCHAR(36) UNIQUE
        name        VARCHAR
      table 'times'
        uuid        VARCHAR(36) UNIQUE
        joinTime    BIGINT
        time        BIGINT
        server      ntext
        isAFK       int
     */

    boolean openConnection() throws Exception;

    boolean isClosed();

    void close() throws Exception;

    void init() throws SQLException;

    void putTime(UUID player, long joinTime, long time, String server, boolean isAFK) throws SQLException;




}

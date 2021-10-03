package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import java.sql.*;
import java.util.UUID;

public class MySQLDatabase implements Database {
    private final String database;
    private final String username;
    private final String password;
    private final String address;
    private Connection connection;

    public MySQLDatabase(String address, String database, String username, String password) {
        this.address = address;
        this.database = database;
        this.username = username;
        this.password = password;
    }


    @Override
    public boolean openConnection() throws Exception {
        if (!isClosed())
            throw new IllegalStateException("already opened");

        String url = "jdbc:mysql://" + address + "/" + database;
        connection = DriverManager.getConnection(url, username, password);
        return true;
    }

    @Override
    public boolean isClosed() {
        if (connection == null)
            return true;

        try {
            return connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    @Override
    public void close() throws Exception {
        if (!isClosed()) {
            connection.close();
            connection = null;
        }
    }


    @Override
    public void init() throws SQLException {
        if (isClosed())
            throw new IllegalStateException("connection is closed");

        String sql = "CREATE TABLE IF NOT EXISTS users (uuid VARCHAR(36) UNIQUE, name nTEXT);";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }

        sql = "CREATE TABLE IF NOT EXISTS times (uuid VARCHAR(36) UNIQUE, joinTime BIGINT, time BIGINT, server nText, isAFK INT);";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public void putTime(UUID player, long joinTime, long time, String server, boolean isAFK) throws SQLException {
        if (isClosed())
            throw new IllegalStateException("connection is closed");

        String sql = "INSERT INTO 'users' VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(0, player.toString());
            stmt.setLong(1, joinTime);
            stmt.setLong(2, time);
            stmt.setNString(3, server);
            stmt.setBoolean(4, isAFK);
            stmt.executeUpdate();
        }

    }

}


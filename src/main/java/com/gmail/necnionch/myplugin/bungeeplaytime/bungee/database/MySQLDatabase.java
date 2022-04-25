package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class MySQLDatabase implements Database {
    private final String database;
    private final String username;
    private final String password;
    private final String address;
    private final Map<String, String> dbOptions;
    private final Logger log;
    private Connection connection;
    private final Map<UUID, String> cachedPlayerNames = Maps.newConcurrentMap();


    public MySQLDatabase(String address, String database, String username, String password, Map<String, String> dbOptions, Logger logger) {
        this.address = address;
        this.database = database;
        this.username = username;
        this.password = password;
        this.log = logger;
        this.dbOptions = dbOptions;
    }

    public MySQLDatabase(String address, String database, String username, String password, Logger logger) {
        this(address, database, username, password, Collections.emptyMap(), logger);
    }


    @Override
    public boolean openConnection() throws SQLException {
        if (!isClosed())
            throw new IllegalStateException("already opened");

        String url = "jdbc:mysql://" + address + "/" + database;
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        dbOptions.forEach(properties::setProperty);
        connection = DriverManager.getConnection(url, properties);
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

        String sql = "CREATE TABLE IF NOT EXISTS users (uuid VARCHAR(36) UNIQUE, name TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }

        sql = "CREATE TABLE IF NOT EXISTS times (uuid VARCHAR(36), startTime BIGINT, time BIGINT, server TEXT, isAFK INT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public void putTime(UUID playerId, String playerName, long startTime, long time, String server, boolean isAFK) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "INSERT INTO `times` VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setLong(2, startTime);
            stmt.setLong(3, time);
            stmt.setNString(4, server);
            stmt.setBoolean(5, isAFK);
            stmt.executeUpdate();
        }

        // update name
        if (!cachedPlayerNames.containsKey(playerId)) {  // no cached
//            log.warning("no cached -> fetch...");
            try {
                fetchPlayerName(playerId)
                    .ifPresent(s -> {
                        cachedPlayerNames.put(playerId, s);
//                        log.warning("  no fetched");
                    });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!playerName.equals(cachedPlayerNames.getOrDefault(playerId, null))) {
            putPlayerName(playerId, playerName);
        }

    }

    @Override
    public Optional<LookupPlayerResult> lookupTime(UUID playerId) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "SELECT "
                + "SUM(case when `isAFK` = 0 then `time` else 0 end) AS played, "
                + "SUM(case when `isAFK` = 1 then `time` else 0 end) AS afk "
                + "FROM `times` WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long played = rs.getLong(1);
                long afk = rs.getLong(2);
                return Optional.of(new LookupPlayerResult(playerId, played, afk));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<LookupPlayerResult> lookupTime(UUID playerId, long afters) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "SELECT "
                + "SUM(case when `isAFK` = 0 then `time` else 0 end) AS played, "
                + "SUM(case when `isAFK` = 1 then `time` else 0 end) AS afk "
                + "FROM `times` WHERE uuid = ? AND startTime > ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setLong(2, afters);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long played = rs.getLong(1);
                long afk = rs.getLong(2);
                return Optional.of(new LookupPlayerResult(playerId, played, afk));
            }
        }
        return Optional.empty();
    }

    @Override
    public LookupTop lookupTimeTops(int lookupCount, boolean afks) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        if (lookupCount <= 0)
            throw new IllegalArgumentException("lookupCount > 0");

        String sql = ""
                + "SELECT "
                + "  `uuid`, "
                + "  SUM(`time`) AS total, "
                + "  SUM(case when `isAFK` = 0 then `time` else 0 end) AS played, "
                + "  SUM(case when `isAFK` = 1 then `time` else 0 end) AS afk "
                + "FROM `times` "
                + "GROUP BY `uuid` "
                + "ORDER BY `" + ((afks) ? "total" : "played") + "` DESC "
                + "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, lookupCount);

            ResultSet rs = stmt.executeQuery();

            List<LookupTop.Entry> entries = Lists.newArrayList();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                long played = rs.getLong("played");
                long afk = rs.getLong("afk");

                LookupTop.Entry entry = new LookupTop.Entry(uuid, played, afk);
                entries.add(entry);
            }

            return new LookupTop(Collections.unmodifiableList(entries));
        }
    }

    @Override
    public LookupTop lookupTimeTops(int lookupCount, boolean afks, long afters) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        if (lookupCount <= 0)
            throw new IllegalArgumentException("lookupCount > 0");

        String targetColumn = (afks) ? "total" : "played";
        String sql = "SELECT "
                + "`uuid`, "
                + "SUM(`time`) AS total, "
                + "SUM(case when `isAFK` = 0 then `time` else 0 end) AS played, "
                + "SUM(case when `isAFK` = 1 then `time` else 0 end) AS afk "
                + "FROM `times` "
                + "WHERE `" + targetColumn + "` > ? "
                + "ORDER BY `" + targetColumn + "` "
                + "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, afters);
            stmt.setInt(2, lookupCount);

            ResultSet rs = stmt.executeQuery();

            List<LookupTop.Entry> entries = Lists.newArrayList();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                long played = rs.getLong("played");
                long afk = rs.getLong("afk");

                LookupTop.Entry entry = new LookupTop.Entry(uuid, played, afk);
                entries.add(entry);
            }

            return new LookupTop(Collections.unmodifiableList(entries));
        }
    }


    private Optional<String> fetchPlayerName(UUID playerId) throws SQLException {
        if (isClosed())
            return Optional.empty();

        String sql = "SELECT name, uuid FROM `users` WHERE uuid=? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getNString("name"));
            }
        }
        return Optional.empty();
    }

    private Optional<PlayerId> fetchPlayerId(String playerName) throws SQLException {
        if (isClosed())
            return Optional.empty();

        String sql = "SELECT uuid, name FROM `users` WHERE LOWER(name) = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerName.toLowerCase(Locale.ROOT));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UUID uuid;
                String sUUID = rs.getNString("uuid");
                String name = rs.getNString("name");
                try {
                    uuid = UUID.fromString(sUUID);
                } catch (IllegalArgumentException e) {
                    log.warning("UUID parsing error: " + sUUID);
                    return Optional.empty();
                }

                cachedPlayerNames.put(uuid, name);
                return Optional.of(new PlayerId(uuid, name));
            }
        }
        return Optional.empty();
    }

    private void putPlayerName(UUID playerId, String playerName) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "DELETE FROM `users` WHERE uuid=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        }

        sql = "INSERT INTO `users` VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, playerName);
            stmt.executeUpdate();
        }
        cachedPlayerNames.put(playerId, playerName);
    }


    @Override
    public Optional<PlayerId> getPlayerName(UUID playerId) throws SQLException {
        if (!cachedPlayerNames.containsKey(playerId)) {
            String name = fetchPlayerName(playerId).orElse(null);
            if (name != null) {
                cachedPlayerNames.put(playerId, name);
                return Optional.of(new PlayerId(playerId, name));
            }
            return Optional.empty();
        }

        return Optional.of(new PlayerId(playerId, cachedPlayerNames.get(playerId)));
    }

    @Override
    public Optional<PlayerId> getPlayerId(String playerName) throws SQLException {
        Optional<Map.Entry<UUID, String>> cached = cachedPlayerNames.entrySet()
                .stream()
                .filter(e -> e.getValue().equalsIgnoreCase(playerName))
                .findFirst();
        if (cached.isPresent())
            return Optional.of(new PlayerId(cached.get().getKey(), cached.get().getValue()));

        return fetchPlayerId(playerName);
    }

}


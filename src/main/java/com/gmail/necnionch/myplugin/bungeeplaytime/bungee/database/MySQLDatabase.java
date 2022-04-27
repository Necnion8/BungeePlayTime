package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.AFKState;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.MainConfig;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerName;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeEntries;
import com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result.PlayerTimeResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
    private final Map<UUID, String> cachedPlayerNames = Collections.synchronizedMap(Maps.newLinkedHashMap());


    public MySQLDatabase(String address, String database, String username, String password, Map<String, String> dbOptions, Logger logger) {
        this.address = address;
        this.database = database;
        this.username = username;
        this.password = password;
        this.log = logger;
        this.dbOptions = dbOptions;
    }

    public MySQLDatabase(MainConfig.Database settings, Logger logger) {
        this(settings.getAddress(), settings.getDatabase(), settings.getUserName(), settings.getPassword(), settings.getOptions(), logger);
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

        String sql = "CREATE TABLE IF NOT EXISTS users (uuid VARCHAR(36) UNIQUE, name TEXT, lastTime BIGINT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }

        sql = "CREATE TABLE IF NOT EXISTS times (uuid VARCHAR(36), startTime BIGINT, time BIGINT, server TEXT, isAFK INT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }

        // v2: add columns
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, "users", "lastTime")) {
            if (!rs.next()) {
                sql = "ALTER TABLE `users` ADD `lastTime` BIGINT NOT NULL DEFAULT 0";
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate(sql);
                }
            }
        }
    }

    @Override
    public void putTime(UUID playerId, String playerName, long startTime, long time, String server, AFKState state) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "INSERT INTO `times` VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setLong(2, startTime);
            stmt.setLong(3, time);
            stmt.setNString(4, server);
            stmt.setInt(5, state.getValue());
            stmt.executeUpdate();
        }

        putPlayerName(playerId, playerName);

    }

    @Override
    public Optional<PlayerTimeResult> lookupTime(UUID playerId, long afters) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = ""
                + "SELECT "
                + "  SUM(case when `isAFK` = 0 then `time` else 0 end) AS `played`, "
                + "  SUM(case when `isAFK` = 1 then `time` else 0 end) AS `afk`, "
                + "  SUM(case when `isAFK` = -1 then `time` else 0 end) AS `unknown` "
                + "FROM `times` "
                + "WHERE `uuid` = ? AND `startTime` > ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setLong(2, afters);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long played = rs.getLong("played");
                long afk = rs.getLong("afk");
                long unknown = rs.getLong("unknown");
                return Optional.of(new PlayerTimeResult(playerId, played, afk, unknown));
            }
        }
        return Optional.empty();
    }

    @Override
    public PlayerTimeEntries lookupTimeTops(int count, int offset, boolean totalTime, long afters) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        if (count <= 0)
            throw new IllegalArgumentException("count > 0");
        if (offset < 0)
            throw new IllegalArgumentException("offset >= 0");

        String targetColumn = (totalTime) ? "total" : "played";
        String sql = ""
                + "SELECT "
                + "  `uuid`, "
                + "  SUM(`time`) AS `total`, "
                + "  SUM(case when `isAFK` = 0 then `time` else 0 end) AS `played`, "
                + "  SUM(case when `isAFK` = 1 then `time` else 0 end) AS `afk`, "
                + "  SUM(case when `isAFK` = -1 then `time` else 0 end) AS `unknown` "
                + "FROM `times` "
                + "WHERE `startTime` > ? "
                + "GROUP BY `uuid` "
                + "ORDER BY `" + targetColumn + "` DESC "
                + "LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, afters);
            stmt.setInt(2, count);
            stmt.setInt(3, offset);
            List<PlayerTimeResult> entries = Lists.newArrayList();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    long played = rs.getLong("played");
                    long afk = rs.getLong("afk");
                    long unknown = rs.getLong("unknown");

                    PlayerTimeResult entry = new PlayerTimeResult(uuid, played, afk, unknown);
                    entries.add(entry);
                }
            }

            return new PlayerTimeEntries(Collections.unmodifiableList(entries));
        }
    }

    @Override
    public OptionalInt lookupTimeRanking(UUID playerId, boolean totalTime, long afters) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        // exists check
        boolean found;
        String sql = "SELECT `uuid` FROM `times` WHERE `uuid` = ?";
        if (!totalTime)
            sql += " AND `isAFK` = 0";
        sql += " LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                found = rs.next();
            }
        }
        if (!found)
            return OptionalInt.empty();

        // search ranking
        String target = (totalTime) ? "SUM(`time`) AS `result`" : "SUM(case when `isAFK` = 0 then `time` else 0 end) AS `result`";
        sql = ""
                + "SELECT "
                + "  COUNT(*) AS `rank`"
                + "FROM ("
                + "  SELECT"
                + "    `uuid`,"
                + "    " + target
                + "  FROM"
                + "    `times`"
                + "  GROUP BY"
                + "    `uuid`"
                + "  HAVING"
                + "    `result` > ("
                + "      SELECT"
                + "        " + target
                + "      FROM"
                + "        `times`"
                + "      WHERE"
                + "        `uuid` = ?"
                + "    )"
                + ") AS `totals`";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return OptionalInt.of(rs.getInt("rank"));
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalLong lookupFirstTime(UUID playerId) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "SELECT `startTime` FROM `times` WHERE `uuid` = ? ORDER BY `startTime` ASC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return OptionalLong.of(rs.getLong("startTime"));
            }
        }
        return OptionalLong.empty();
    }

    @Override
    public OptionalLong lookupLastTime(UUID playerId) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "SELECT `startTime`, `time` FROM `times` WHERE `uuid` = ? ORDER BY `startTime` DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return OptionalLong.of(rs.getLong("startTime") + rs.getLong("time"));
            }
        }
        return OptionalLong.empty();
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

    private Optional<PlayerName> fetchPlayerId(String playerName) throws SQLException {
        if (isClosed())
            return Optional.empty();

        String sql = "SELECT * FROM `users` WHERE LOWER(name) = ? ORDER BY `lastTime` DESC LIMIT 1";
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
                return Optional.of(new PlayerName(uuid, name));
            }
        }
        return Optional.empty();
    }

    private void putPlayerName(UUID playerId, String playerName) throws SQLException {
        if (isClosed() && !openConnection())
            throw new IllegalStateException("connection is closed");

        String sql = "INSERT INTO `users` VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?, `lastTime` = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, playerName);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setString(4, playerName);
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        }
        cachedPlayerNames.put(playerId, playerName);
    }


    @Override
    public Optional<PlayerName> getPlayerName(UUID playerId) throws SQLException {
        if (!cachedPlayerNames.containsKey(playerId)) {
            String name = fetchPlayerName(playerId).orElse(null);
            if (name != null) {
                cachedPlayerNames.put(playerId, name);
                return Optional.of(new PlayerName(playerId, name));
            }
            return Optional.empty();
        }

        return Optional.of(new PlayerName(playerId, cachedPlayerNames.get(playerId)));
    }

    @Override
    public Optional<PlayerName> getPlayerId(String playerName) throws SQLException {
        Optional<Map.Entry<UUID, String>> cached = Sets.newLinkedHashSet(cachedPlayerNames.entrySet())
                .stream()
                .filter(e -> e.getValue().equalsIgnoreCase(playerName))
                .findFirst();
        if (cached.isPresent())
            return Optional.of(new PlayerName(cached.get().getKey(), cached.get().getValue()));

        return fetchPlayerId(playerName);
    }


    public Map<UUID, String> cachedPlayerNames() {
        return Collections.unmodifiableMap(cachedPlayerNames);
    }

}


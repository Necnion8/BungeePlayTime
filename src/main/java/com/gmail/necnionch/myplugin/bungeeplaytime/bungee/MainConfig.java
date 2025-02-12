package com.gmail.necnionch.myplugin.bungeeplaytime.bungee;

import com.gmail.necnionch.myplugin.bungeeplaytime.common.BPTUtil;
import com.gmail.necnionch.myplugin.bungeeplaytime.common.BungeeConfigDriver;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainConfig extends BungeeConfigDriver {
    private final Players playersSection = new Players();
    private final Database mySqlSection = new Database("mysql");
    private final Database sqliteSection = new Database("sqlite");
    private final Logger log;

    public MainConfig(Plugin plugin) {
        super(plugin);
        log = plugin.getLogger();
    }

    public Players getPlayers() {
        return playersSection;
    }

    public Database getMySQL() {
        return mySqlSection;
    }

    public Database getSQLite() {
        return sqliteSection;
    }

    public DBType getDatabaseType() {
        try {
            return DBType.valueOf(Optional.ofNullable(config.getString("db-type", "mysql")).orElse("mysql").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warning("Invalid db-type: " + e.getMessage());
            return DBType.MYSQL;
        }
    }

    public boolean isConnectorPluginSupport() {
        return config.getBoolean("enable-connector-plugin-support", true);
    }


    @Override
    public boolean onLoaded(Configuration config) {
        if (super.onLoaded(config)) {
            BPTUtil.setPlayedInUnknownState(playersSection.isPlayedInUnknownState());
            return true;
        }
        return false;
    }


    public final class Players extends ChildSection {
        public Players() {
            super("players");
        }

        public boolean isPlayedInUnknownState() {
            return get().getBoolean("played-in-unknown-state");
        }

        public int getAFKMinutes() {
            return Math.max(1, get().getInt("afk-minutes", 5));
        }

    }

    public final class Database extends ChildSection {
        public Database(String dbType) {
            super("database." + dbType);
        }

        public String getAddress() {
            return get().getString("address", "");
        }

        public String getDatabase() {
            return get().getString("database", "");
        }

        public String getUserName() {
            return get().getString("username", "");
        }

        public String getPassword() {
            return get().getString("password", "");
        }

        public Map<String, String> getOptions() {
            Configuration section = get().getSection("options");
            if (section != null)
                return section.getKeys().stream()
                        .collect(Collectors.toMap(k -> k, k -> String.valueOf(section.get(k))));
            return Collections.emptyMap();
        }

    }


    private class ChildSection {
        private final String key;

        public ChildSection(String key) {
            this.key = key;
        }

        public Configuration get() {
            return config.getSection(key);
        }
    }

    public enum DBType {
        SQLITE, MYSQL
    }

}

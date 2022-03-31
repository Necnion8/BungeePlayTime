package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LookupTop {

    private final List<Entry> entries;

    public LookupTop(List<Entry> entries) {
        this.entries = entries;

    }

    public List<Entry> getEntries() {
        return entries;
    }

    public CompletableFuture<Map<UUID, Optional<String>>> fetchNames(NameLookup lookup) {
        CompletableFuture<Map<UUID, Optional<String>>> f = new CompletableFuture<>();
        if (entries.isEmpty()) {
            f.complete(Collections.emptyMap());
        } else {
            Set<UUID> waited = entries.stream()
                    .map(Entry::getUniqueId)
                    .distinct()
                    .collect(Collectors.toCollection(Sets::newConcurrentHashSet));
            Map<UUID, Optional<String>> names = Maps.newConcurrentMap();

            entries.forEach(e -> {
                lookup.fetchPlayerName(e.getUniqueId())
                        .whenComplete((name, ex) -> {
                            waited.remove(e.getUniqueId());
                            if (ex == null) {
                                names.put(e.getUniqueId(), name.map(PlayerId::getName));
                            }

                            if (waited.isEmpty()) {
                                f.complete(names);
                            }
                        });
            });
        }
        return f;
    }





    public static class Entry {

        private final UUID uniqueId;
        private final long playedTime;
        private final long afkTime;

        public Entry(UUID uniqueId, long playedTime, long afkTime) {
            this.uniqueId = uniqueId;
            this.playedTime = playedTime;
            this.afkTime = afkTime;
        }

        public UUID getUniqueId() {
            return uniqueId;
        }

        public long getPlayedTime() {
            return playedTime;
        }

        public long getAFKTime() {
            return afkTime;
        }

    }

    public interface NameLookup {
        CompletableFuture<Optional<PlayerId>> fetchPlayerName(UUID playerId);
    }

}

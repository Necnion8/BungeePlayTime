package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.database.result;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerTimeEntries {

    private final List<PlayerTimeResult> entries;

    public PlayerTimeEntries(List<PlayerTimeResult> entries) {
        this.entries = entries;

    }

    public List<PlayerTimeResult> getEntries() {
        return entries;
    }

    public CompletableFuture<Map<UUID, Optional<String>>> fetchNames(NameLookup lookup) {
        CompletableFuture<Map<UUID, Optional<String>>> f = new CompletableFuture<>();
        if (entries.isEmpty()) {
            f.complete(Collections.emptyMap());
        } else {
            Set<UUID> waited = entries.stream()
                    .map(PlayerTimeResult::getPlayerId)
                    .distinct()
                    .collect(Collectors.toCollection(Sets::newConcurrentHashSet));
            Map<UUID, Optional<String>> names = Maps.newConcurrentMap();

            entries.forEach(e -> {
                lookup.fetchPlayerName(e.getPlayerId())
                        .whenComplete((name, ex) -> {
                            waited.remove(e.getPlayerId());
                            if (ex == null) {
                                names.put(e.getPlayerId(), name.map(PlayerName::getName));
                            }

                            if (waited.isEmpty()) {
                                f.complete(names);
                            }
                        });
            });
        }
        return f;
    }


    public interface NameLookup {
        CompletableFuture<Optional<PlayerName>> fetchPlayerName(UUID playerId);
    }

}

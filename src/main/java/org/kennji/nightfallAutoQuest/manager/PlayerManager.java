package org.kennji.nightfallAutoQuest.manager;

import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.model.PlayerData;
import org.kennji.nightfallAutoQuest.repository.PlayerRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerManager {
    private final PlayerRepository repository;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerManager(@NotNull PlayerRepository repository) {
        this.repository = repository;
    }

    public void loadPlayer(@NotNull UUID uuid) {
        repository.load(uuid).thenAccept(data -> cache.put(uuid, data));
    }

    public @NotNull PlayerData getPlayerData(@NotNull UUID uuid) {
        return cache.getOrDefault(uuid, new PlayerData(uuid, 0, 0, null, null, 0, 0, 0, 0));
    }

    public void updatePlayerData(@NotNull UUID uuid, @NotNull PlayerData data) {
        cache.put(uuid, data);
    }

    public void savePlayer(@NotNull UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            repository.save(data);
        }
    }

    public void saveAll() {
        cache.keySet().forEach(this::savePlayer);
    }

    public void unloadPlayer(@NotNull UUID uuid) {
        savePlayer(uuid);
        cache.remove(uuid);
    }
}

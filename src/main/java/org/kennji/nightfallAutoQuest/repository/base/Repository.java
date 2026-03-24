package org.kennji.nightfallAutoQuest.repository.base;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

public interface Repository<K, V> {
    @NotNull CompletableFuture<V> load(@NotNull K key);
    @NotNull CompletableFuture<Void> save(@NotNull V value);
    @NotNull CompletableFuture<Void> delete(@NotNull K key);
}

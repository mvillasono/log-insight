package io.github.mvillasono.loginsight.core.deduplication;

import io.github.mvillasono.loginsight.core.pipeline.DeduplicationStore;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryDeduplicationStore implements DeduplicationStore {

    private record Entry(Instant expiresAt, AtomicInteger count) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public boolean isDuplicate(String fingerprint) {
        Entry entry = store.get(fingerprint);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(fingerprint);
            return false;
        }
        entry.count().incrementAndGet();
        return true;
    }

    @Override
    public void markSeen(String fingerprint, Duration window) {
        store.put(fingerprint, new Entry(Instant.now().plus(window), new AtomicInteger(1)));
    }

    @Override
    public int getOccurrences(String fingerprint) {
        Entry entry = store.get(fingerprint);
        return entry == null ? 0 : entry.count().get();
    }

    /** Elimina entradas expiradas — útil para llamar periódicamente y evitar memory leak. */
    public void evictExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }
}

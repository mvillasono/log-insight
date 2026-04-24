package io.github.mvillasono.loginsight.core.deduplication;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDeduplicationStoreTest {

    @Test
    void newFingerprintIsNotDuplicate() {
        InMemoryDeduplicationStore store = new InMemoryDeduplicationStore();
        assertThat(store.isDuplicate("fp-abc")).isFalse();
    }

    @Test
    void afterMarkSeenIsDuplicate() {
        InMemoryDeduplicationStore store = new InMemoryDeduplicationStore();
        store.markSeen("fp-abc", Duration.ofMinutes(10));
        assertThat(store.isDuplicate("fp-abc")).isTrue();
    }

    @Test
    void occurrencesIncrementOnDuplicateCheck() {
        InMemoryDeduplicationStore store = new InMemoryDeduplicationStore();
        store.markSeen("fp-abc", Duration.ofMinutes(10));

        store.isDuplicate("fp-abc");
        store.isDuplicate("fp-abc");

        assertThat(store.getOccurrences("fp-abc")).isGreaterThanOrEqualTo(2);
    }

    @Test
    void expiredEntryIsNoLongerDuplicate() throws InterruptedException {
        InMemoryDeduplicationStore store = new InMemoryDeduplicationStore();
        store.markSeen("fp-exp", Duration.ofMillis(50));

        Thread.sleep(100);

        assertThat(store.isDuplicate("fp-exp")).isFalse();
    }

    @Test
    void evictExpiredRemovesStaleEntries() throws InterruptedException {
        InMemoryDeduplicationStore store = new InMemoryDeduplicationStore();
        store.markSeen("fp-stale", Duration.ofMillis(50));

        Thread.sleep(100);
        store.evictExpired();

        assertThat(store.getOccurrences("fp-stale")).isZero();
    }
}

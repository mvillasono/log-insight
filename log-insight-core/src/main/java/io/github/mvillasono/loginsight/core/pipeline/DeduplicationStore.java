package io.github.mvillasono.loginsight.core.pipeline;

import java.time.Duration;

public interface DeduplicationStore {

    /**
     * Returns true if this fingerprint was marked seen within its configured window.
     */
    boolean isDuplicate(String fingerprint);

    void markSeen(String fingerprint, Duration window);

    int getOccurrences(String fingerprint);
}

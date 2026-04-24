package io.github.mvillasono.loginsight.autoconfigure.pipeline;

import io.github.mvillasono.loginsight.autoconfigure.config.LogInsightProperties;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiterService {

    private final LogInsightProperties properties;

    private final AtomicInteger minuteCount = new AtomicInteger(0);
    private final AtomicInteger hourCount   = new AtomicInteger(0);
    private volatile Instant minuteWindowStart = Instant.now();
    private volatile Instant hourWindowStart   = Instant.now();

    public RateLimiterService(LogInsightProperties properties) {
        this.properties = properties;
    }

    public synchronized boolean tryAcquire() {
        Instant now = Instant.now();
        resetWindowsIfNeeded(now);

        int maxPerMinute = properties.getRateLimit().getMaxPerMinute();
        int maxPerHour   = properties.getRateLimit().getMaxPerHour();

        if (minuteCount.get() >= maxPerMinute) return false;
        if (hourCount.get()   >= maxPerHour)   return false;

        minuteCount.incrementAndGet();
        hourCount.incrementAndGet();
        return true;
    }

    private void resetWindowsIfNeeded(Instant now) {
        if (now.isAfter(minuteWindowStart.plusSeconds(60))) {
            minuteCount.set(0);
            minuteWindowStart = now;
        }
        if (now.isAfter(hourWindowStart.plusSeconds(3600))) {
            hourCount.set(0);
            hourWindowStart = now;
        }
    }
}

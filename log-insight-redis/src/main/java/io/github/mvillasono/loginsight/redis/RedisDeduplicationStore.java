package io.github.mvillasono.loginsight.redis;

import io.github.mvillasono.loginsight.core.pipeline.DeduplicationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Deduplicación compartida entre instancias vía Redis.
 * Reemplaza automáticamente a InMemoryDeduplicationStore cuando
 * spring-data-redis está en el classpath y Redis está disponible.
 */
public class RedisDeduplicationStore implements DeduplicationStore {

    private static final Logger log = LoggerFactory.getLogger(RedisDeduplicationStore.class);

    private static final String KEY_DEDUP = "log-insight:dedup:";
    private static final String KEY_COUNT = "log-insight:count:";

    private final StringRedisTemplate redis;

    public RedisDeduplicationStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean isDuplicate(String fingerprint) {
        try {
            Boolean exists = redis.hasKey(KEY_DEDUP + fingerprint);
            if (Boolean.TRUE.equals(exists)) {
                redis.opsForValue().increment(KEY_COUNT + fingerprint);
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.warn("[LogInsight] Redis isDuplicate failed, treating as non-duplicate: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void markSeen(String fingerprint, Duration window) {
        try {
            redis.opsForValue().set(KEY_DEDUP + fingerprint, "1", window);
            redis.opsForValue().set(KEY_COUNT + fingerprint, "1", window);
        } catch (Exception ex) {
            log.warn("[LogInsight] Redis markSeen failed: {}", ex.getMessage());
        }
    }

    @Override
    public int getOccurrences(String fingerprint) {
        try {
            String val = redis.opsForValue().get(KEY_COUNT + fingerprint);
            return val == null ? 0 : Integer.parseInt(val);
        } catch (Exception ex) {
            log.warn("[LogInsight] Redis getOccurrences failed: {}", ex.getMessage());
            return 0;
        }
    }
}

package io.github.mvillasono.loginsight.redis;

import io.github.mvillasono.loginsight.core.pipeline.DeduplicationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Se registra ANTES de LogInsightAutoConfiguration para que el bean de Redis
 * sea visible cuando LogInsightAutoConfiguration evalúa @ConditionalOnMissingBean.
 * Usamos beforeName para evitar dependencia circular entre los módulos.
 */
@AutoConfiguration(beforeName = "io.github.mvillasono.loginsight.autoconfigure.LogInsightAutoConfiguration")
@ConditionalOnClass(StringRedisTemplate.class)
public class LogInsightRedisAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LogInsightRedisAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean(DeduplicationStore.class)
    public DeduplicationStore redisDeduplicationStore(StringRedisTemplate redisTemplate) {
        log.info("[LogInsight] Redis deduplication enabled — shared state across instances");
        return new RedisDeduplicationStore(redisTemplate);
    }
}

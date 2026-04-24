package io.github.mvillasono.loginsight.autoconfigure.pipeline;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.mvillasono.loginsight.autoconfigure.ai.AiAnalysisService;
import io.github.mvillasono.loginsight.autoconfigure.appender.LogInsightPipelineHolder;
import io.github.mvillasono.loginsight.autoconfigure.config.LogInsightProperties;
import io.github.mvillasono.loginsight.autoconfigure.event.LogInsightEvent;
import io.github.mvillasono.loginsight.core.model.LogAnalysis;
import io.github.mvillasono.loginsight.core.model.LogEvent;
import io.github.mvillasono.loginsight.core.pipeline.DeduplicationStore;
import io.github.mvillasono.loginsight.core.sanitization.SanitizationChain;
import io.github.mvillasono.loginsight.core.util.FingerprintGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogAnalysisPipeline {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisPipeline.class);

    private final SanitizationChain sanitizationChain;
    private final DeduplicationStore deduplicationStore;
    private final RateLimiterService rateLimiter;
    private final AiAnalysisService aiService;
    private final ApplicationEventPublisher eventPublisher;
    private final LogInsightProperties properties;
    private final String serviceName;

    private volatile boolean running = false;
    private Thread processorThread;

    public LogAnalysisPipeline(SanitizationChain sanitizationChain,
                               DeduplicationStore deduplicationStore,
                               RateLimiterService rateLimiter,
                               AiAnalysisService aiService,
                               ApplicationEventPublisher eventPublisher,
                               LogInsightProperties properties,
                               String serviceName) {
        this.sanitizationChain  = sanitizationChain;
        this.deduplicationStore = deduplicationStore;
        this.rateLimiter        = rateLimiter;
        this.aiService          = aiService;
        this.eventPublisher     = eventPublisher;
        this.properties         = properties;
        this.serviceName        = serviceName;
    }

    @PostConstruct
    public void start() {
        running = true;
        processorThread = new Thread(this::processLoop, "log-insight-processor");
        processorThread.setDaemon(true);
        processorThread.start();
        log.info("[LogInsight] Pipeline started — provider={}, model={}",
                properties.getAi().getProvider(), properties.getAi().getModel());
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (processorThread != null) processorThread.interrupt();
    }

    private void processLoop() {
        BlockingQueue<ILoggingEvent> queue = LogInsightPipelineHolder.getQueue();
        while (running) {
            try {
                ILoggingEvent raw = queue.poll(1, TimeUnit.SECONDS);
                if (raw != null) process(raw);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void process(ILoggingEvent raw) {
        try {
            String sanitizedMessage    = sanitizationChain.sanitize(raw.getFormattedMessage());
            String sanitizedStackTrace = sanitizationChain.sanitize(extractStackTrace(raw));
            List<String> sanitizedCtx  = sanitizationChain.sanitizeAll(List.of());

            String fingerprint = FingerprintGenerator.generate(
                    raw.getLevel().toString(),
                    raw.getLoggerName(),
                    sanitizedStackTrace
            );

            if (properties.getDeduplication().isEnabled() && deduplicationStore.isDuplicate(fingerprint)) {
                log.debug("[LogInsight] Duplicate skipped fingerprint={}", fingerprint);
                return;
            }

            if (!rateLimiter.tryAcquire()) {
                log.debug("[LogInsight] Rate limit reached, event dropped");
                return;
            }

            Map<String, String> mdc = raw.getMDCPropertyMap();

            LogEvent event = LogEvent.builder()
                    .serviceName(serviceName)
                    .level(raw.getLevel().toString())
                    .loggerName(raw.getLoggerName())
                    .message(sanitizedMessage)
                    .stackTrace(sanitizedStackTrace)
                    .contextLines(sanitizedCtx)
                    .timestamp(Instant.ofEpochMilli(raw.getTimeStamp()))
                    .fingerprint(fingerprint)
                    .httpMethod(mdc.getOrDefault("http.method", ""))
                    .httpPath(mdc.getOrDefault("http.path", ""))
                    .build();

            if (properties.getDeduplication().isEnabled()) {
                deduplicationStore.markSeen(fingerprint, properties.getDeduplication().getWindow());
            }

            int occurrences = deduplicationStore.getOccurrences(fingerprint);
            LogAnalysis analysis = aiService.analyze(event, occurrences);
            eventPublisher.publishEvent(new LogInsightEvent(this, analysis));

        } catch (Exception ex) {
            log.warn("[LogInsight] Error processing log event: {}", ex.getMessage());
        }
    }

    private String extractStackTrace(ILoggingEvent event) {
        if (event.getThrowableProxy() == null) return "";
        int maxLines = properties.getCapture().getMaxStackLines();
        return Arrays.stream(event.getThrowableProxy().getStackTraceElementProxyArray())
                .limit(maxLines)
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }


}

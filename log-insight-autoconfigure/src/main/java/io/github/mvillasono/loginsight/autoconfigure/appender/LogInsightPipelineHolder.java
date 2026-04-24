package io.github.mvillasono.loginsight.autoconfigure.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Puente entre el Logback Appender (inicializado antes que Spring)
 * y el pipeline gestionado por Spring.
 */
public final class LogInsightPipelineHolder {

    private static final BlockingQueue<ILoggingEvent> QUEUE = new LinkedBlockingQueue<>(2000);

    private LogInsightPipelineHolder() {}

    static boolean offer(ILoggingEvent event) {
        return QUEUE.offer(event);
    }

    public static BlockingQueue<ILoggingEvent> getQueue() {
        return QUEUE;
    }
}

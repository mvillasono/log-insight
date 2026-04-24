package io.github.mvillasono.loginsight.autoconfigure.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.Set;

public class LogInsightAppender extends AppenderBase<ILoggingEvent> {

    private final Set<String> captureLevels;

    public LogInsightAppender(Set<String> captureLevels) {
        this.captureLevels = captureLevels;
        setName("LogInsightAppender");
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!captureLevels.contains(event.getLevel().toString())) return;
        // No bloqueamos el hilo de la aplicación — descartamos si la cola está llena
        LogInsightPipelineHolder.offer(event);
    }
}

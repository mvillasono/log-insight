package io.github.mvillasono.loginsight.ui.api;

import io.github.mvillasono.loginsight.core.model.LogAnalysis;

import java.time.Instant;
import java.util.List;

public record AnalysisDto(
        String fingerprint,
        String service,
        String severity,
        String level,
        String logger,
        String rootCause,
        String analysis,
        List<String> suggestions,
        String message,
        String stackTrace,
        int occurrences,
        Instant timestamp,
        Instant analyzedAt,
        String httpMethod,
        String httpPath
) {
    public static AnalysisDto from(LogAnalysis a) {
        return new AnalysisDto(
                a.event().fingerprint(),
                a.event().serviceName(),
                a.severity().name(),
                a.event().level(),
                a.event().loggerName(),
                a.rootCause(),
                a.analysis(),
                a.suggestions(),
                a.event().message(),
                a.event().stackTrace(),
                a.occurrences(),
                a.event().timestamp(),
                a.analyzedAt(),
                a.event().httpMethod(),
                a.event().httpPath()
        );
    }
}

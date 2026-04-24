package io.github.mvillasono.loginsight.ui.api;

import io.github.mvillasono.loginsight.core.model.LogAnalysis;
import io.github.mvillasono.loginsight.core.model.Severity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record StatsDto(int critical, int high, int medium, int low, int total) {

    public static StatsDto from(List<LogAnalysis> analyses) {
        Map<Severity, Long> counts = analyses.stream()
                .collect(Collectors.groupingBy(LogAnalysis::severity, Collectors.counting()));

        int critical = counts.getOrDefault(Severity.CRITICAL, 0L).intValue();
        int high     = counts.getOrDefault(Severity.HIGH,     0L).intValue();
        int medium   = counts.getOrDefault(Severity.MEDIUM,   0L).intValue();
        int low      = counts.getOrDefault(Severity.LOW,      0L).intValue();

        return new StatsDto(critical, high, medium, low, analyses.size());
    }
}

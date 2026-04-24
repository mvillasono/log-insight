package io.github.mvillasono.loginsight.autoconfigure.sink;

import io.github.mvillasono.loginsight.autoconfigure.event.LogInsightEvent;
import io.github.mvillasono.loginsight.core.model.LogAnalysis;
import org.springframework.context.event.EventListener;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsoleSinkListener {

    private static final String SEPARATOR = "═".repeat(60);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @EventListener
    public void onAnalysis(LogInsightEvent event) {
        LogAnalysis analysis = event.getAnalysis();
        System.out.println();
        System.out.println("[LOG-INSIGHT] " + SEPARATOR);
        System.out.printf("  Service     : %s%n", analysis.event().serviceName());
        System.out.printf("  Severity    : %s%n", analysis.severity());
        System.out.printf("  Occurrences : %d%n", analysis.occurrences());
        System.out.printf("  Timestamp   : %s%n", FMT.format(analysis.analyzedAt()));
        System.out.println();
        System.out.printf("  Root Cause  : %s%n", analysis.rootCause());
        System.out.println();
        System.out.printf("  Analysis    : %s%n", analysis.analysis());
        System.out.println();
        printSuggestions(analysis.suggestions());
        System.out.println("[LOG-INSIGHT] " + SEPARATOR);
        System.out.println();
    }

    private void printSuggestions(List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) return;
        System.out.println("  Suggestions :");
        for (int i = 0; i < suggestions.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, suggestions.get(i));
        }
        System.out.println();
    }
}

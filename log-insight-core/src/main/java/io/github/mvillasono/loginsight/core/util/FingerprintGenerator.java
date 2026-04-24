package io.github.mvillasono.loginsight.core.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class FingerprintGenerator {

    private static final int STACK_LINES_FOR_FINGERPRINT = 3;

    private FingerprintGenerator() {}

    /**
     * Genera un fingerprint estable para deduplicación basado en el nivel,
     * el logger y las primeras líneas del stack trace.
     */
    public static String generate(String level, String loggerName, String stackTrace) {
        String stackSummary = firstLines(stackTrace, STACK_LINES_FOR_FINGERPRINT);
        String raw = level + ":" + loggerName + ":" + stackSummary;
        return Integer.toHexString(raw.hashCode());
    }

    private static String firstLines(String text, int max) {
        if (text == null || text.isBlank()) return "";
        return Arrays.stream(text.split("\\n"))
                .limit(max)
                .map(String::trim)
                .collect(Collectors.joining("|"));
    }
}

package io.github.mvillasono.loginsight.core.sanitization;

import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;

import java.util.regex.Pattern;

public class UuidSanitizationRule implements SanitizationRule {

    private static final Pattern UUID = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        return UUID.matcher(input).replaceAll("[UUID REDACTED]");
    }

    @Override
    public String name() { return "uuids"; }
}

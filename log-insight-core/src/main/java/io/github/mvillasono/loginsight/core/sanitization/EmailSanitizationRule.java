package io.github.mvillasono.loginsight.core.sanitization;

import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;

import java.util.regex.Pattern;

public class EmailSanitizationRule implements SanitizationRule {

    private static final Pattern EMAIL = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        return EMAIL.matcher(input).replaceAll("[EMAIL REDACTED]");
    }

    @Override
    public String name() { return "email"; }
}

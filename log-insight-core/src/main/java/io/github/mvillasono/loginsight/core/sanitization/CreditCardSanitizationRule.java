package io.github.mvillasono.loginsight.core.sanitization;

import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;

import java.util.regex.Pattern;

public class CreditCardSanitizationRule implements SanitizationRule {

    // 13–19 dígitos, opcionalmente separados por espacios o guiones
    private static final Pattern CARD = Pattern.compile(
            "\\b(?:\\d[ \\-]?){12,18}\\d\\b"
    );

    @Override
    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        return CARD.matcher(input).replaceAll("[CARD REDACTED]");
    }

    @Override
    public String name() { return "credit-cards"; }
}

package io.github.mvillasono.loginsight.core.sanitization;

import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;

import java.util.Objects;
import java.util.regex.Pattern;

public class CustomPatternSanitizationRule implements SanitizationRule {

    private final String ruleName;
    private final Pattern pattern;
    private final String replacement;

    public CustomPatternSanitizationRule(String ruleName, String regex, String replacement) {
        this.ruleName    = Objects.requireNonNull(ruleName, "ruleName");
        this.pattern     = Pattern.compile(Objects.requireNonNull(regex, "regex"));
        this.replacement = Objects.requireNonNullElse(replacement, "[REDACTED]");
    }

    @Override
    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        return pattern.matcher(input).replaceAll(replacement);
    }

    @Override
    public String name() { return ruleName; }
}

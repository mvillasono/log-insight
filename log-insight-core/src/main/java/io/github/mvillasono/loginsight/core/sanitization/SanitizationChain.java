package io.github.mvillasono.loginsight.core.sanitization;

import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SanitizationChain {

    private final List<SanitizationRule> rules;

    public SanitizationChain(List<SanitizationRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        String result = input;
        for (SanitizationRule rule : rules) {
            result = rule.sanitize(result);
        }
        return result;
    }

    public List<String> sanitizeAll(List<String> lines) {
        if (lines == null) return List.of();
        List<String> sanitized = new ArrayList<>(lines.size());
        for (String line : lines) {
            sanitized.add(sanitize(line));
        }
        return sanitized;
    }

    public int ruleCount() { return rules.size(); }
}

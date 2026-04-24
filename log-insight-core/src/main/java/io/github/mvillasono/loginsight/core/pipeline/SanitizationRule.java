package io.github.mvillasono.loginsight.core.pipeline;

@FunctionalInterface
public interface SanitizationRule {

    String sanitize(String input);

    default String name() {
        return getClass().getSimpleName();
    }
}

package io.github.mvillasono.loginsight.autoconfigure.ai;

import io.github.mvillasono.loginsight.core.model.LogEvent;

import java.util.stream.Collectors;

final class PromptBuilder {

    private static final String TEMPLATE = """
            You are an expert Spring Boot / Java microservices engineer.
            Analyze the following sanitized log error and respond ONLY with valid JSON.
            Write ALL text fields (rootCause, analysis, suggestions) in %s.

            JSON format required:
            {
              "rootCause": "<one concise sentence describing the root cause>",
              "severity": "<CRITICAL | HIGH | MEDIUM | LOW>",
              "analysis": "<2-4 sentences explaining what happened and why>",
              "suggestions": ["<action 1>", "<action 2>", "<action 3>"]
            }

            Severity guide:
            - CRITICAL: data loss, system down, security breach
            - HIGH: feature broken, affecting users
            - MEDIUM: degraded performance, partial failure
            - LOW: warning, non-blocking issue

            ── Error context ───────────────────────────────────────────────
            Service   : %s
            Level     : %s
            Logger    : %s
            Timestamp : %s
            HTTP      : %s %s

            Message:
            %s

            Stack trace:
            %s

            Recent log context:
            %s
            ────────────────────────────────────────────────────────────────
            """;

    private PromptBuilder() {}

    static String build(LogEvent event, String language) {
        String context = event.contextLines().isEmpty()
                ? "(none)"
                : event.contextLines().stream().collect(Collectors.joining("\n"));

        String httpInfo = (event.httpMethod() != null && !event.httpMethod().isBlank())
                ? event.httpMethod() + " " + event.httpPath()
                : "(no HTTP context)";

        return TEMPLATE.formatted(
                language,
                event.serviceName(),
                event.level(),
                event.loggerName(),
                event.timestamp(),
                event.httpMethod().isBlank() ? "" : event.httpMethod(),
                event.httpPath().isBlank() ? "(no HTTP context)" : event.httpPath(),
                event.message(),
                event.stackTrace(),
                context
        );
    }
}

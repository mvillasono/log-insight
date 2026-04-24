package io.github.mvillasono.loginsight.autoconfigure.ai;

import io.github.mvillasono.loginsight.core.model.LogAnalysis;
import io.github.mvillasono.loginsight.core.model.LogEvent;
import io.github.mvillasono.loginsight.core.model.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    private final ChatClient chatClient;
    private final String language;

    public AiAnalysisService(ChatClient chatClient, String language) {
        this.chatClient = chatClient;
        this.language   = language;
    }

    public LogAnalysis analyze(LogEvent event, int occurrences) {
        try {
            String prompt = PromptBuilder.build(event, language);

            AiAnalysisResponse response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(AiAnalysisResponse.class);

            return toLogAnalysis(event, response, occurrences);

        } catch (Exception ex) {
            log.warn("[LogInsight] AI analysis failed for fingerprint={}: {}", event.fingerprint(), ex.getMessage());
            return fallbackAnalysis(event, occurrences);
        }
    }

    private LogAnalysis toLogAnalysis(LogEvent event, AiAnalysisResponse response, int occurrences) {
        return LogAnalysis.builder()
                .event(event)
                .rootCause(response.rootCause())
                .severity(parseSeverity(response.severity()))
                .analysis(response.analysis())
                .suggestions(response.suggestions() != null ? response.suggestions() : List.of())
                .occurrences(occurrences)
                .build();
    }

    private LogAnalysis fallbackAnalysis(LogEvent event, int occurrences) {
        return LogAnalysis.builder()
                .event(event)
                .rootCause("Analysis unavailable")
                .severity(Severity.MEDIUM)
                .analysis("AI analysis could not be completed. Review the original log for details.")
                .suggestions(List.of("Check AI provider configuration and API key"))
                .occurrences(occurrences)
                .build();
    }

    private Severity parseSeverity(String raw) {
        if (raw == null) return Severity.MEDIUM;
        try {
            return Severity.valueOf(raw.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return Severity.MEDIUM;
        }
    }
}

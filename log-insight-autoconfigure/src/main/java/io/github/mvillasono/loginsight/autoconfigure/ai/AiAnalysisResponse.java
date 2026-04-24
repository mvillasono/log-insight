package io.github.mvillasono.loginsight.autoconfigure.ai;

import java.util.List;

/**
 * Estructura que Spring AI deserializa directamente desde la respuesta del modelo.
 */
public record AiAnalysisResponse(
        String rootCause,
        String severity,
        String analysis,
        List<String> suggestions
) {}

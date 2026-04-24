package io.github.mvillasono.loginsight.ui.api;

import io.github.mvillasono.loginsight.autoconfigure.store.AnalysisStore;
import io.github.mvillasono.loginsight.core.model.LogAnalysis;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/log-insight/api")
public class LogInsightApiController {

    private final AnalysisStore store;

    public LogInsightApiController(AnalysisStore store) {
        this.store = store;
    }

    @GetMapping("/analyses")
    public List<AnalysisDto> getAnalyses() {
        return store.getAll().stream()
                .map(AnalysisDto::from)
                .toList();
    }

    @GetMapping("/stats")
    public StatsDto getStats() {
        List<LogAnalysis> all = store.getAll();
        return StatsDto.from(all);
    }
}

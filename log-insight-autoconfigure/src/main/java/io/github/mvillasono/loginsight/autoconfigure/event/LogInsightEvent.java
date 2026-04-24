package io.github.mvillasono.loginsight.autoconfigure.event;

import io.github.mvillasono.loginsight.core.model.LogAnalysis;
import io.github.mvillasono.loginsight.core.model.Severity;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class LogInsightEvent extends ApplicationEvent {

    private final LogAnalysis analysis;

    public LogInsightEvent(Object source, LogAnalysis analysis) {
        super(source);
        this.analysis = analysis;
    }

    public LogAnalysis getAnalysis()       { return analysis; }
    public String getOriginalMessage()     { return analysis.event().message(); }
    public String getRootCause()           { return analysis.rootCause(); }
    public String getAnalysisText()        { return analysis.analysis(); }
    public List<String> getSuggestions()   { return analysis.suggestions(); }
    public Severity getSeverity()          { return analysis.severity(); }
    public String getServiceName()         { return analysis.event().serviceName(); }
    public int getOccurrences()            { return analysis.occurrences(); }
}

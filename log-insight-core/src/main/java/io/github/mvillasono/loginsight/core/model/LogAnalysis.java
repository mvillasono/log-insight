package io.github.mvillasono.loginsight.core.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class LogAnalysis {

    private final LogEvent event;
    private final String analysis;
    private final String rootCause;
    private final List<String> suggestions;
    private final Severity severity;
    private final Instant analyzedAt;
    private final int occurrences;

    private LogAnalysis(Builder builder) {
        this.event       = Objects.requireNonNull(builder.event, "event");
        this.analysis    = Objects.requireNonNullElse(builder.analysis, "");
        this.rootCause   = Objects.requireNonNullElse(builder.rootCause, "");
        this.suggestions = Objects.requireNonNullElse(builder.suggestions, List.of());
        this.severity    = Objects.requireNonNullElse(builder.severity, Severity.MEDIUM);
        this.analyzedAt  = Objects.requireNonNullElse(builder.analyzedAt, Instant.now());
        this.occurrences = Math.max(1, builder.occurrences);
    }

    public LogEvent event()            { return event; }
    public String analysis()           { return analysis; }
    public String rootCause()          { return rootCause; }
    public List<String> suggestions()  { return suggestions; }
    public Severity severity()         { return severity; }
    public Instant analyzedAt()        { return analyzedAt; }
    public int occurrences()           { return occurrences; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private LogEvent event;
        private String analysis;
        private String rootCause;
        private List<String> suggestions;
        private Severity severity;
        private Instant analyzedAt;
        private int occurrences = 1;

        public Builder event(LogEvent v)            { this.event = v; return this; }
        public Builder analysis(String v)           { this.analysis = v; return this; }
        public Builder rootCause(String v)          { this.rootCause = v; return this; }
        public Builder suggestions(List<String> v)  { this.suggestions = v; return this; }
        public Builder severity(Severity v)         { this.severity = v; return this; }
        public Builder analyzedAt(Instant v)        { this.analyzedAt = v; return this; }
        public Builder occurrences(int v)           { this.occurrences = v; return this; }

        public LogAnalysis build() { return new LogAnalysis(this); }
    }
}

package io.github.mvillasono.loginsight.core.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LogEvent {

    private final String serviceName;
    private final String level;
    private final String loggerName;
    private final String message;
    private final String stackTrace;
    private final List<String> contextLines;
    private final Instant timestamp;
    private final String fingerprint;
    private final String httpMethod;
    private final String httpPath;

    private LogEvent(Builder builder) {
        this.serviceName  = Objects.requireNonNull(builder.serviceName, "serviceName");
        this.level        = Objects.requireNonNull(builder.level, "level");
        this.loggerName   = Objects.requireNonNullElse(builder.loggerName, "unknown");
        this.message      = Objects.requireNonNullElse(builder.message, "");
        this.stackTrace   = Objects.requireNonNullElse(builder.stackTrace, "");
        this.contextLines = Collections.unmodifiableList(new ArrayList<>(builder.contextLines));
        this.timestamp    = Objects.requireNonNullElse(builder.timestamp, Instant.now());
        this.fingerprint  = Objects.requireNonNull(builder.fingerprint, "fingerprint");
        this.httpMethod   = Objects.requireNonNullElse(builder.httpMethod, "");
        this.httpPath     = Objects.requireNonNullElse(builder.httpPath, "");
    }

    public String serviceName()   { return serviceName; }
    public String level()         { return level; }
    public String loggerName()    { return loggerName; }
    public String message()       { return message; }
    public String stackTrace()    { return stackTrace; }
    public List<String> contextLines() { return contextLines; }
    public Instant timestamp()    { return timestamp; }
    public String fingerprint()   { return fingerprint; }
    public String httpMethod()    { return httpMethod; }
    public String httpPath()      { return httpPath; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String serviceName;
        private String level;
        private String loggerName;
        private String message;
        private String stackTrace;
        private List<String> contextLines = new ArrayList<>();
        private Instant timestamp;
        private String fingerprint;
        private String httpMethod;
        private String httpPath;

        public Builder serviceName(String v)       { this.serviceName = v; return this; }
        public Builder level(String v)             { this.level = v; return this; }
        public Builder loggerName(String v)        { this.loggerName = v; return this; }
        public Builder message(String v)           { this.message = v; return this; }
        public Builder stackTrace(String v)        { this.stackTrace = v; return this; }
        public Builder contextLines(List<String> v){ this.contextLines = v; return this; }
        public Builder timestamp(Instant v)        { this.timestamp = v; return this; }
        public Builder fingerprint(String v)       { this.fingerprint = v; return this; }
        public Builder httpMethod(String v)        { this.httpMethod = v; return this; }
        public Builder httpPath(String v)          { this.httpPath = v; return this; }

        public LogEvent build() { return new LogEvent(this); }
    }
}

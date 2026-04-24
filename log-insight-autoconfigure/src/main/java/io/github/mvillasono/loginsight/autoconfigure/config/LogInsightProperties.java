package io.github.mvillasono.loginsight.autoconfigure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "log-insight")
public class LogInsightProperties {

    private boolean enabled = true;
    private String defaultOutput = "console";
    private AiProperties ai = new AiProperties();
    private CaptureProperties capture = new CaptureProperties();
    private SanitizationProperties sanitization = new SanitizationProperties();
    private DeduplicationProperties deduplication = new DeduplicationProperties();
    private RateLimitProperties rateLimit = new RateLimitProperties();
    private UiProperties ui = new UiProperties();
    private SinksProperties sinks = new SinksProperties();

    // ── AI ──────────────────────────────────────────────────────────────────

    public static class AiProperties {
        private String provider = "openai";
        private String model = "gpt-4o-mini";
        private String language = "English";

        public String getProvider()          { return provider; }
        public void setProvider(String v)    { this.provider = v; }
        public String getModel()             { return model; }
        public void setModel(String v)       { this.model = v; }
        public String getLanguage()          { return language; }
        public void setLanguage(String v)    { this.language = v; }
    }

    // ── Capture ─────────────────────────────────────────────────────────────

    public static class CaptureProperties {
        private List<String> levels = List.of("ERROR", "WARN");
        private int maxStackLines = 30;
        private int contextLines = 10;

        public List<String> getLevels()          { return levels; }
        public void setLevels(List<String> v)    { this.levels = v; }
        public int getMaxStackLines()            { return maxStackLines; }
        public void setMaxStackLines(int v)      { this.maxStackLines = v; }
        public int getContextLines()             { return contextLines; }
        public void setContextLines(int v)       { this.contextLines = v; }
    }

    // ── Sanitización ────────────────────────────────────────────────────────

    public static class SanitizationProperties {
        private boolean enabled = true;
        private BuiltInRules builtIn = new BuiltInRules();
        private List<CustomRule> custom = new ArrayList<>();

        public boolean isEnabled()              { return enabled; }
        public void setEnabled(boolean v)       { this.enabled = v; }
        public BuiltInRules getBuiltIn()        { return builtIn; }
        public void setBuiltIn(BuiltInRules v)  { this.builtIn = v; }
        public List<CustomRule> getCustom()     { return custom; }
        public void setCustom(List<CustomRule> v) { this.custom = v; }

        public static class BuiltInRules {
            private boolean emails = true;
            private boolean creditCards = true;
            private boolean jwtTokens = true;
            private boolean ipAddresses = false;
            private boolean uuids = false;

            public boolean isEmails()              { return emails; }
            public void setEmails(boolean v)       { this.emails = v; }
            public boolean isCreditCards()         { return creditCards; }
            public void setCreditCards(boolean v)  { this.creditCards = v; }
            public boolean isJwtTokens()           { return jwtTokens; }
            public void setJwtTokens(boolean v)    { this.jwtTokens = v; }
            public boolean isIpAddresses()         { return ipAddresses; }
            public void setIpAddresses(boolean v)  { this.ipAddresses = v; }
            public boolean isUuids()               { return uuids; }
            public void setUuids(boolean v)        { this.uuids = v; }
        }

        public static class CustomRule {
            private String name;
            private String pattern;
            private String replacement = "[REDACTED]";

            public String getName()              { return name; }
            public void setName(String v)        { this.name = v; }
            public String getPattern()           { return pattern; }
            public void setPattern(String v)     { this.pattern = v; }
            public String getReplacement()       { return replacement; }
            public void setReplacement(String v) { this.replacement = v; }
        }
    }

    // ── Deduplicación ───────────────────────────────────────────────────────

    public static class DeduplicationProperties {
        private boolean enabled = true;
        private Duration window = Duration.ofMinutes(10);

        public boolean isEnabled()           { return enabled; }
        public void setEnabled(boolean v)    { this.enabled = v; }
        public Duration getWindow()          { return window; }
        public void setWindow(Duration v)    { this.window = v; }
    }

    // ── Rate Limit ──────────────────────────────────────────────────────────

    public static class RateLimitProperties {
        private int maxPerMinute = 5;
        private int maxPerHour = 50;

        public int getMaxPerMinute()           { return maxPerMinute; }
        public void setMaxPerMinute(int v)     { this.maxPerMinute = v; }
        public int getMaxPerHour()             { return maxPerHour; }
        public void setMaxPerHour(int v)       { this.maxPerHour = v; }
    }

    // ── UI ──────────────────────────────────────────────────────────────────

    public static class UiProperties {
        private boolean enabled = true;
        private String path = "/log-insight";

        public boolean isEnabled()        { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getPath()           { return path; }
        public void setPath(String v)     { this.path = v; }
    }

    // ── Sinks ───────────────────────────────────────────────────────────────

    public static class SinksProperties {
        private ConsoleSinkProperties console = new ConsoleSinkProperties();
        private SlackSinkProperties slack = new SlackSinkProperties();
        private WebhookSinkProperties webhook = new WebhookSinkProperties();
        private ActuatorSinkProperties actuator = new ActuatorSinkProperties();

        public ConsoleSinkProperties getConsole()         { return console; }
        public void setConsole(ConsoleSinkProperties v)   { this.console = v; }
        public SlackSinkProperties getSlack()             { return slack; }
        public void setSlack(SlackSinkProperties v)       { this.slack = v; }
        public WebhookSinkProperties getWebhook()         { return webhook; }
        public void setWebhook(WebhookSinkProperties v)   { this.webhook = v; }
        public ActuatorSinkProperties getActuator()       { return actuator; }
        public void setActuator(ActuatorSinkProperties v) { this.actuator = v; }

        public static class ConsoleSinkProperties {
            private boolean enabled = true;
            public boolean isEnabled()        { return enabled; }
            public void setEnabled(boolean v) { this.enabled = v; }
        }

        public static class SlackSinkProperties {
            private boolean enabled = false;
            private String webhookUrl;
            private String channel = "#alerts";
            public boolean isEnabled()           { return enabled; }
            public void setEnabled(boolean v)    { this.enabled = v; }
            public String getWebhookUrl()        { return webhookUrl; }
            public void setWebhookUrl(String v)  { this.webhookUrl = v; }
            public String getChannel()           { return channel; }
            public void setChannel(String v)     { this.channel = v; }
        }

        public static class WebhookSinkProperties {
            private boolean enabled = false;
            private String url;
            private Map<String, String> headers = Map.of();
            public boolean isEnabled()               { return enabled; }
            public void setEnabled(boolean v)        { this.enabled = v; }
            public String getUrl()                   { return url; }
            public void setUrl(String v)             { this.url = v; }
            public Map<String, String> getHeaders()  { return headers; }
            public void setHeaders(Map<String, String> v) { this.headers = v; }
        }

        public static class ActuatorSinkProperties {
            private boolean enabled = true;
            private int maxHistory = 50;
            public boolean isEnabled()         { return enabled; }
            public void setEnabled(boolean v)  { this.enabled = v; }
            public int getMaxHistory()         { return maxHistory; }
            public void setMaxHistory(int v)   { this.maxHistory = v; }
        }
    }

    // ── Root getters/setters ─────────────────────────────────────────────────

    public boolean isEnabled()                       { return enabled; }
    public void setEnabled(boolean v)                { this.enabled = v; }
    public String getDefaultOutput()                 { return defaultOutput; }
    public void setDefaultOutput(String v)           { this.defaultOutput = v; }
    public AiProperties getAi()                      { return ai; }
    public void setAi(AiProperties v)               { this.ai = v; }
    public CaptureProperties getCapture()            { return capture; }
    public void setCapture(CaptureProperties v)      { this.capture = v; }
    public SanitizationProperties getSanitization()  { return sanitization; }
    public void setSanitization(SanitizationProperties v) { this.sanitization = v; }
    public DeduplicationProperties getDeduplication(){ return deduplication; }
    public void setDeduplication(DeduplicationProperties v) { this.deduplication = v; }
    public RateLimitProperties getRateLimit()         { return rateLimit; }
    public void setRateLimit(RateLimitProperties v)  { this.rateLimit = v; }
    public UiProperties getUi()                      { return ui; }
    public void setUi(UiProperties v)                { this.ui = v; }
    public SinksProperties getSinks()                { return sinks; }
    public void setSinks(SinksProperties v)          { this.sinks = v; }
}

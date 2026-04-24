package io.github.mvillasono.loginsight.autoconfigure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.github.mvillasono.loginsight.autoconfigure.ai.AiAnalysisService;
import io.github.mvillasono.loginsight.autoconfigure.appender.LogInsightAppender;
import io.github.mvillasono.loginsight.autoconfigure.config.LogInsightProperties;
import io.github.mvillasono.loginsight.autoconfigure.pipeline.LogAnalysisPipeline;
import io.github.mvillasono.loginsight.autoconfigure.pipeline.RateLimiterService;
import io.github.mvillasono.loginsight.autoconfigure.filter.LogInsightRequestFilter;
import io.github.mvillasono.loginsight.autoconfigure.sink.ConsoleSinkListener;
import io.github.mvillasono.loginsight.autoconfigure.store.AnalysisStore;
import io.github.mvillasono.loginsight.core.deduplication.InMemoryDeduplicationStore;
import io.github.mvillasono.loginsight.core.pipeline.DeduplicationStore;
import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;
import io.github.mvillasono.loginsight.core.sanitization.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(LogInsightProperties.class)
@ConditionalOnProperty(prefix = "log-insight", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogInsightAutoConfiguration {

    private final LogInsightProperties properties;

    public LogInsightAutoConfiguration(LogInsightProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public SanitizationChain sanitizationChain() {
        List<SanitizationRule> rules = new ArrayList<>();
        LogInsightProperties.SanitizationProperties config = properties.getSanitization();

        if (!config.isEnabled()) return new SanitizationChain(rules);

        LogInsightProperties.SanitizationProperties.BuiltInRules builtIn = config.getBuiltIn();
        if (builtIn.isEmails())      rules.add(new EmailSanitizationRule());
        if (builtIn.isCreditCards()) rules.add(new CreditCardSanitizationRule());
        if (builtIn.isJwtTokens())   rules.add(new JwtSanitizationRule());
        if (builtIn.isIpAddresses()) rules.add(new IpAddressSanitizationRule());
        if (builtIn.isUuids())       rules.add(new UuidSanitizationRule());

        for (LogInsightProperties.SanitizationProperties.CustomRule custom : config.getCustom()) {
            rules.add(new CustomPatternSanitizationRule(
                    custom.getName(), custom.getPattern(), custom.getReplacement()
            ));
        }

        return new SanitizationChain(rules);
    }

    @Bean
    @ConditionalOnMissingBean
    public DeduplicationStore deduplicationStore() {
        return new InMemoryDeduplicationStore();
    }

    @Bean
    public RateLimiterService rateLimiterService() {
        return new RateLimiterService(properties);
    }

    @Bean
    public AiAnalysisService aiAnalysisService(ChatClient.Builder chatClientBuilder) {
        return new AiAnalysisService(chatClientBuilder.build(), properties.getAi().getLanguage());
    }

    @Bean
    public LogAnalysisPipeline logAnalysisPipeline(SanitizationChain sanitizationChain,
                                                   DeduplicationStore deduplicationStore,
                                                   RateLimiterService rateLimiterService,
                                                   AiAnalysisService aiAnalysisService,
                                                   ApplicationEventPublisher eventPublisher,
                                                   Environment env) {
        String serviceName = env.getProperty("spring.application.name", "unknown-service");
        return new LogAnalysisPipeline(
                sanitizationChain, deduplicationStore, rateLimiterService,
                aiAnalysisService, eventPublisher, properties, serviceName
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "log-insight.sinks.console", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public ConsoleSinkListener consoleSinkListener() {
        return new ConsoleSinkListener();
    }

    @Bean
    public FilterRegistrationBean<LogInsightRequestFilter> logInsightRequestFilter() {
        FilterRegistrationBean<LogInsightRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LogInsightRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public AnalysisStore analysisStore() {
        return new AnalysisStore(properties.getSinks().getActuator().getMaxHistory());
    }

    /** Registra el appender en Logback una vez que el contexto de Spring está listo. */
    @Bean
    public AppenderRegistrar logInsightAppenderRegistrar(LogAnalysisPipeline pipeline) {
        return new AppenderRegistrar(properties, pipeline);
    }

    public static class AppenderRegistrar {

        private final LogInsightProperties properties;

        public AppenderRegistrar(LogInsightProperties properties, LogAnalysisPipeline pipeline) {
            this.properties = properties;
        }

        @PostConstruct
        public void register() {
            if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) return;

            LogInsightAppender appender = new LogInsightAppender(
                    new HashSet<>(properties.getCapture().getLevels())
            );
            appender.setContext(context);
            appender.start();

            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
            root.addAppender(appender);
        }
    }
}

package io.github.mvillasono.loginsight.ui;

import io.github.mvillasono.loginsight.autoconfigure.store.AnalysisStore;
import io.github.mvillasono.loginsight.ui.api.LogInsightApiController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnProperty(prefix = "log-insight.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(AnalysisStore.class)
public class LogInsightUiAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /log-insight → /log-insight/index.html (servido como recurso estático)
        registry.addRedirectViewController("/log-insight", "/log-insight/index.html");
    }

    @Bean
    public LogInsightApiController logInsightApiController(AnalysisStore store) {
        return new LogInsightApiController(store);
    }
}

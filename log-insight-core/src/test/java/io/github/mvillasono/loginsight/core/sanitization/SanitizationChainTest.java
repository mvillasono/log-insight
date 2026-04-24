package io.github.mvillasono.loginsight.core.sanitization;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanitizationChainTest {

    @Test
    void appliesAllRulesInOrder() {
        SanitizationChain chain = new SanitizationChain(List.of(
                new EmailSanitizationRule(),
                new JwtSanitizationRule()
        ));

        String input = "User user@example.com used token eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String result = chain.sanitize(input);

        assertThat(result).doesNotContain("user@example.com");
        assertThat(result).doesNotContain("eyJhbGci");
        assertThat(result).contains("[EMAIL REDACTED]");
        assertThat(result).contains("[JWT REDACTED]");
    }

    @Test
    void sanitizeAllHandlesNullList() {
        SanitizationChain chain = new SanitizationChain(List.of(new EmailSanitizationRule()));
        assertThat(chain.sanitizeAll(null)).isEmpty();
    }

    @Test
    void sanitizeHandlesNullInput() {
        SanitizationChain chain = new SanitizationChain(List.of(new EmailSanitizationRule()));
        assertThat(chain.sanitize(null)).isNull();
    }

    @Test
    void emptyChainReturnsInputUnchanged() {
        SanitizationChain chain = new SanitizationChain(List.of());
        assertThat(chain.sanitize("hello world")).isEqualTo("hello world");
    }
}

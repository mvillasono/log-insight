package io.github.mvillasono.loginsight.core.sanitization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltInRulesTest {

    @Test
    void emailRuleMasksSimpleEmail() {
        String result = new EmailSanitizationRule().sanitize("Contacto: user@example.com");
        assertThat(result).doesNotContain("user@example.com");
        assertThat(result).contains("[EMAIL REDACTED]");
    }

    @Test
    void jwtRuleMasksToken() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String result = new JwtSanitizationRule().sanitize("Authorization: Bearer " + jwt);
        assertThat(result).doesNotContain("eyJhbGci");
        assertThat(result).contains("[JWT REDACTED]");
    }

    @Test
    void creditCardRuleMasksNumber() {
        String result = new CreditCardSanitizationRule().sanitize("Card: 4111 1111 1111 1111");
        assertThat(result).doesNotContain("4111");
        assertThat(result).contains("[CARD REDACTED]");
    }

    @Test
    void ipRuleMasksAddress() {
        String result = new IpAddressSanitizationRule().sanitize("Request from 192.168.1.100");
        assertThat(result).doesNotContain("192.168.1.100");
        assertThat(result).contains("[IP REDACTED]");
    }

    @Test
    void uuidRuleMasksUuid() {
        String result = new UuidSanitizationRule().sanitize("userId=550e8400-e29b-41d4-a716-446655440000");
        assertThat(result).doesNotContain("550e8400");
        assertThat(result).contains("[UUID REDACTED]");
    }

    @Test
    void customPatternRuleMasksMatch() {
        CustomPatternSanitizationRule rule = new CustomPatternSanitizationRule(
                "internal-id", "userId=\\d+", "userId=[REDACTED]"
        );
        String result = rule.sanitize("Processing userId=98765 failed");
        assertThat(result).doesNotContain("98765");
        assertThat(result).contains("userId=[REDACTED]");
    }

    @Test
    void rulesAreNoOpOnBlankInput() {
        String blank = "   ";
        assertThat(new EmailSanitizationRule().sanitize(blank)).isEqualTo(blank);
        assertThat(new JwtSanitizationRule().sanitize(blank)).isEqualTo(blank);
        assertThat(new CreditCardSanitizationRule().sanitize(blank)).isEqualTo(blank);
    }
}

package io.github.mvillasono.loginsight.core.sanitization;

import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;

import java.util.regex.Pattern;

public class JwtSanitizationRule implements SanitizationRule {

    // JWT: tres segmentos base64url separados por puntos, el primero empieza con eyJ
    private static final Pattern JWT = Pattern.compile(
            "eyJ[A-Za-z0-9+/=_-]+\\.[A-Za-z0-9+/=_-]+\\.[A-Za-z0-9+/=_-]+"
    );

    @Override
    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        return JWT.matcher(input).replaceAll("[JWT REDACTED]");
    }

    @Override
    public String name() { return "jwt-tokens"; }
}

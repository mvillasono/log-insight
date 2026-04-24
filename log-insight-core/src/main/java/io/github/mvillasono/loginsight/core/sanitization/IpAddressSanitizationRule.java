package io.github.mvillasono.loginsight.core.sanitization;

import io.github.mvillasono.loginsight.core.pipeline.SanitizationRule;

import java.util.regex.Pattern;

public class IpAddressSanitizationRule implements SanitizationRule {

    private static final Pattern IPV4 = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\b"
    );

    @Override
    public String sanitize(String input) {
        if (input == null || input.isBlank()) return input;
        return IPV4.matcher(input).replaceAll("[IP REDACTED]");
    }

    @Override
    public String name() { return "ip-addresses"; }
}

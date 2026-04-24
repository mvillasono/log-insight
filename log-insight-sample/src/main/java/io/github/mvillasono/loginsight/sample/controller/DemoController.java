package io.github.mvillasono.loginsight.sample.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints de demo que generan distintos tipos de errores.
 * Úsalos para ver el dashboard de Log Insight en acción.
 *
 * GET /demo/null-pointer      → NullPointerException
 * GET /demo/database-timeout  → connection timeout simulado
 * GET /demo/auth-error        → error de autenticación con datos sensibles
 * GET /demo/format-error      → NumberFormatException
 * GET /demo/warn              → solo un WARN (sin stack trace)
 * GET /demo/status            → health check — sin errores
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    // ── Endpoints de demo ────────────────────────────────────────

    @GetMapping("/null-pointer")
    public String nullPointer() {
        log.info("Processing user request for userId=98765");
        String user = fetchUser(null);           // retorna null a propósito
        return user.toUpperCase();               // → NPE
    }

    @GetMapping("/database-timeout")
    public String databaseTimeout() {
        try {
            simulateDatabaseQuery("SELECT * FROM orders WHERE userId=42");
        } catch (RuntimeException ex) {
            log.error("Database connection pool exhausted after 30s timeout. Active connections: 50/50", ex);
            throw ex;
        }
        return "ok";
    }

    @GetMapping("/auth-error")
    public String authError() {
        // Log con datos sensibles — la sanitización los enmascarará antes de enviar a la IA
        log.error("JWT validation failed for user@example.com. " +
                "Token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c " +
                "IP: 192.168.1.42");
        throw new SecurityException("Invalid or expired JWT token");
    }

    @GetMapping("/format-error")
    public String formatError() {
        log.warn("Received unexpected value in 'amount' field from external API");
        return String.valueOf(Integer.parseInt("not-a-number"));
    }

    @GetMapping("/warn")
    public String warnOnly() {
        log.warn("Response time exceeded threshold: 3420ms > 2000ms for endpoint /api/orders");
        return "Warning logged — check the dashboard";
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "status", "UP",
                "dashboard", "http://localhost:8080/log-insight",
                "hint", "Call /demo/null-pointer, /demo/database-timeout, /demo/auth-error to generate errors"
        );
    }

    // ── Exception handler ────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handle(Exception ex) {
        // El log.error aquí es lo que captura el Logback appender de Log Insight
        log.error("Demo endpoint threw an exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
    }

    // ── Helpers de demo ──────────────────────────────────────────

    private String fetchUser(String id) {
        return null;
    }

    private void simulateDatabaseQuery(String sql) {
        throw new RuntimeException(
                "HikariPool-1 — Connection is not available, request timed out after 30000ms"
        );
    }
}

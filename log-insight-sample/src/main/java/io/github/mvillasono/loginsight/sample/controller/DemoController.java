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
 * — Severidad por nivel —
 * GET /demo/critical          → corrupción de datos + pérdida de transacciones (CRITICAL)
 * GET /demo/high              → base de datos primaria caída (HIGH)
 * GET /demo/medium            → timeout en API externa con reintentos fallidos (MEDIUM)
 * GET /demo/low               → uso de API deprecada con fallback (LOW)
 *
 * — Otros errores —
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

    // ── Endpoints por severidad ──────────────────────────────────

    @GetMapping("/critical")
    public String critical() {
        log.error("FATAL: Data corruption detected in orders table. " +
                "Transaction rollback FAILED — 847 records may be in inconsistent state. " +
                "Primary key constraint violated during batch insert. " +
                "Database integrity compromised. Immediate DBA intervention required.");
        throw new RuntimeException("Database integrity violation — data loss detected");
    }

    @GetMapping("/high")
    public String high() {
        try {
            simulatePrimaryDbDown();
        } catch (RuntimeException ex) {
            log.error("Primary database unreachable. Failover to replica FAILED. " +
                    "All write operations are rejected. Service is partially degraded. " +
                    "Connection attempts: 5/5 exhausted. Last error: Connection refused at db-primary:5432", ex);
            throw ex;
        }
        return "ok";
    }

    @GetMapping("/medium")
    public String medium() {
        log.error("External payment API timeout after 3 retry attempts. " +
                "Endpoint: https://api.payments.example.com/charge " +
                "Response time: 30000ms (threshold: 5000ms). " +
                "Requests in queue: 12. Falling back to async processing.");
        throw new RuntimeException("Payment gateway timeout — transaction queued for retry");
    }

    @GetMapping("/low")
    public String low() {
        log.warn("Deprecated method UserService.findByEmailLegacy() called from OrderController.checkout(). " +
                "This method will be removed in v3.0.0. " +
                "Migrate to UserService.findByEmail() before next release. " +
                "Fallback applied — functionality unaffected.");
        return "Warning logged — check the dashboard";
    }

    // ── Otros endpoints de demo ──────────────────────────────────

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
                "severity-demos", Map.of(
                        "CRITICAL", "/demo/critical",
                        "HIGH",     "/demo/high",
                        "MEDIUM",   "/demo/medium",
                        "LOW",      "/demo/low"
                )
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

    private void simulatePrimaryDbDown() {
        throw new RuntimeException(
                "Unable to acquire JDBC Connection — db-primary:5432 connection refused"
        );
    }
}

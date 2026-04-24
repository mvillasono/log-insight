# Log Insight

> AI-powered log analysis for Spring Boot microservices

[![Build](https://github.com/mvillasono/log-insight/actions/workflows/ci.yml/badge.svg)](https://github.com/mvillasono/log-insight/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.mvillasono/log-insight-spring-boot-starter)](https://central.sonatype.com/artifact/io.github.mvillasono/log-insight-spring-boot-starter)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-brightgreen)](https://spring.io/projects/spring-ai)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

Log Insight es un Spring Boot Starter que intercepta errores de tus microservicios, los sanitiza automáticamente para proteger datos sensibles, y los envía a un modelo de IA (OpenAI, Anthropic, Ollama, etc.) para obtener análisis de causa raíz y sugerencias concretas — todo disponible en un dashboard web integrado accesible desde tu propia aplicación.

---

## Características

- **Dashboard web integrado** en `/log-insight` — sin instalar nada extra, como Swagger UI
- **Sanitización configurable** antes de enviar datos a la IA (emails, JWT, tarjetas, patrones custom)
- **Deduplicación con Redis** — comparte el estado entre múltiples instancias, no analiza el mismo error dos veces
- **Multi-provider** vía Spring AI — OpenAI, Anthropic, Ollama (local/gratis) y más
- **Control de costos** — rate limiting, deduplicación y límite de líneas por análisis
- **Sinks built-in** — Consola, Slack, Webhook HTTP
- **Extensible** vía `@EventListener` para cualquier integración custom

---

## Quick Start

### 1. Agregar dependencia

**Maven:**
```xml
<dependency>
    <groupId>io.github.mvillasono</groupId>
    <artifactId>log-insight-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Agregar el provider de IA que prefieras -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'io.github.mvillasono:log-insight-spring-boot-starter:1.0.0'
implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0'
```

### 2. Configurar

```yaml
# application.yml
log-insight:
  enabled: true
  ai:
    provider: openai
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. Listo

```
http://localhost:8080/log-insight
```

Los errores de tu aplicación se analizan automáticamente y aparecen en el dashboard.

---

## Configuración completa

```yaml
log-insight:
  enabled: true

  ai:
    provider: openai          # openai | anthropic | ollama
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini        # elige el modelo según tu presupuesto

  capture:
    levels: [ERROR, WARN]     # niveles a capturar
    max-stack-lines: 30       # líneas máximas del stack trace
    context-lines: 10         # líneas de log anteriores al error

  sanitization:
    enabled: true
    built-in:
      emails: true
      credit-cards: true
      jwt-tokens: true
      ip-addresses: false
      uuids: false
    custom:
      - name: "user-id"
        pattern: "userId=\\d+"
        replacement: "userId=[REDACTED]"

  deduplication:
    enabled: true
    window: 10m               # mismo error en 10 min = no re-analizar

  rate-limit:
    max-per-minute: 5
    max-per-hour: 50

  ui:
    enabled: true
    path: /log-insight

  default-output: console     # console | none

  sinks:
    console:
      enabled: true
    slack:
      enabled: false
      webhook-url: ${SLACK_WEBHOOK_URL}
      channel: "#errores-produccion"
    webhook:
      enabled: false
      url: https://hooks.tuempresa.com/alertas
      headers:
        Authorization: Bearer ${WEBHOOK_TOKEN}
    actuator:
      enabled: true
      max-history: 50
```

---

## Providers de IA soportados

| Provider | Starter |
|----------|---------|
| OpenAI | `spring-ai-openai-spring-boot-starter` |
| Anthropic (Claude) | `spring-ai-anthropic-spring-boot-starter` |
| Ollama (local/gratis) | `spring-ai-ollama-spring-boot-starter` |
| Azure OpenAI | `spring-ai-azure-openai-spring-boot-starter` |

---

## Integración custom con @EventListener

```java
@Component
public class MiManejadorDeErrores {

    @EventListener
    public void onAnalysis(LogInsightEvent event) {
        event.getOriginalMessage();  // log sanitizado
        event.getAnalysis();         // explicación en lenguaje natural
        event.getRootCause();        // causa raíz detectada
        event.getSuggestions();      // lista de sugerencias
        event.getSeverity();         // CRITICAL | HIGH | MEDIUM | LOW
        event.getServiceName();      // nombre del microservicio

        if (event.getSeverity() == Severity.CRITICAL) {
            pagerDutyService.alert(event.getAnalysis());
        }
    }
}
```

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                      Tu Spring Boot App                         │
│                                                                 │
│  Exception lanzada → Logback/Log4j2 Appender                   │
│                              │                                  │
│                    ┌─────────▼──────────┐                       │
│                    │   Sanitización     │ built-in + custom     │
│                    └─────────┬──────────┘                       │
│                              │                                  │
│                    ┌─────────▼──────────┐                       │
│                    │  Deduplicación     │ Redis compartido      │
│                    └─────────┬──────────┘                       │
│                              │                                  │
│                    ┌─────────▼──────────┐                       │
│                    │   Rate Limiter     │ por minuto / hora     │
│                    └─────────┬──────────┘                       │
│                              │                                  │
│                    ┌─────────▼──────────┐                       │
│                    │   Cola Async       │ no bloquea el hilo    │
│                    └─────────┬──────────┘                       │
│                              │                                  │
│                    ┌─────────▼──────────┐                       │
│                    │    Spring AI       │ cualquier provider    │
│                    └────┬──────────┬────┘                       │
│                         │          │                            │
│              ┌──────────▼──┐  ┌────▼───────────────────┐       │
│              │LogInsight   │  │  Memoria + Redis        │       │
│              │Event        │  │  (historial compartido) │       │
│              └──────┬──────┘  └────────────┬────────────┘       │
│                     │                      │                    │
│           ┌─────────▼──────┐     ┌─────────▼──────────┐        │
│           │ @EventListener │     │    REST API interna │        │
│           │    (custom)    │     └─────────┬──────────-┘        │
│           └────────────────┘              │                     │
│                                ┌──────────▼──────────┐          │
│                                │  Dashboard Web      │          │
│                                │  /log-insight       │          │
│                                └─────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Estructura del proyecto

```
log-insight/
├── log-insight-core/           # modelos e interfaces — sin Spring, portable
├── log-insight-autoconfigure/  # auto-configuración, appender, Spring AI
├── log-insight-starter/        # starter thin (solo agrega esta dependencia)
├── log-insight-ui/             # dashboard HTML/CSS/JS dentro del JAR
├── log-insight-redis/          # deduplicación compartida entre instancias
├── log-insight-sink-slack/     # sink Slack built-in
├── log-insight-sink-webhook/   # sink Webhook HTTP built-in
└── log-insight-sample/         # app de demo — corre y ve el dashboard en acción
```

---

## Tecnologías

| Tecnología | Versión | Rol |
|-----------|---------|-----|
| Java | 21 | Lenguaje base |
| Spring Boot | 3.4.1 | Framework principal |
| Spring AI | 1.0.0 | Abstracción multi-provider de IA |
| Logback | 1.5.x | Appender para captura de logs |
| Redis / Spring Data Redis | 3.x | Deduplicación entre instancias |
| HTML + CSS + JS (vanilla) | — | Dashboard web sin dependencias extra |
| Maven | 3.9.x | Build y publicación en Maven Central |
| GitHub Actions | — | CI/CD, CodeQL, Dependabot |

---

## Correr el sample en local

```bash
git clone https://github.com/mvillasono/log-insight
cd log-insight
mvn install -DskipTests
cd log-insight-sample
mvn spring-boot:run
```

Abrir en el navegador:
```
http://localhost:8080/log-insight
```

> El sample incluye endpoints que generan errores de ejemplo para ver el dashboard en acción sin necesitar una app real.

---

## Contribuir

¡Las contribuciones son bienvenidas! Lee [CONTRIBUTING.md](CONTRIBUTING.md) antes de empezar.

Áreas donde puedes contribuir:

- Nuevos **sinks** (Teams, PagerDuty, email)
- Nuevas **reglas de sanitización** built-in
- Mejoras al **dashboard UI**
- **Tests** y cobertura
- **Documentación** y ejemplos

---

## Licencia

Apache License 2.0 — ver [LICENSE](LICENSE)

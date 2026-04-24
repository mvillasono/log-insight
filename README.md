# Log Insight

> AI-powered log analysis for Spring Boot microservices

[![Build](https://github.com/mvillasono/log-insight/actions/workflows/ci.yml/badge.svg)](https://github.com/mvillasono/log-insight/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.mvillasono/log-insight-spring-boot-starter)](https://central.sonatype.com/artifact/io.github.mvillasono/log-insight-spring-boot-starter)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-brightgreen)](https://spring.io/projects/spring-ai)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

Log Insight es un Spring Boot Starter que intercepta errores de tus microservicios, los sanitiza automáticamente para proteger datos sensibles, y los envía a un modelo de IA (OpenAI, Anthropic, Ollama, etc.) para obtener análisis de causa raíz y sugerencias concretas — todo disponible en un dashboard web integrado accesible desde tu propia aplicación.

**Disponible en Maven Central:** [io.github.mvillasono » log-insight-spring-boot-starter](https://central.sonatype.com/artifact/io.github.mvillasono/log-insight-spring-boot-starter)

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

> **Requisito obligatorio:** Log Insight necesita que declares en tu proyecto el starter del proveedor de IA que vayas a usar. Sin él, Spring no puede crear el bean `ChatClient.Builder` y la aplicación no arrancará. Ver la [tabla de providers](#providers-de-ia-soportados) para elegir el que corresponda.

**Maven:**
```xml
<dependency>
    <groupId>io.github.mvillasono</groupId>
    <artifactId>log-insight-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>

<!-- OBLIGATORIO: elige el starter del provider que vayas a usar -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'io.github.mvillasono:log-insight-spring-boot-starter:1.0.1'
// OBLIGATORIO: elige el starter del provider que vayas a usar
implementation 'org.springframework.ai:spring-ai-starter-model-openai:1.0.0'
```

### 2. Configurar

> **Importante:** La API key del proveedor de IA se configura bajo `spring.ai.<provider>.api-key`, **no** bajo `log-insight.ai`. Log Insight delega la conexión al proveedor a Spring AI.

```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}   # requerido por Spring AI OpenAI
      chat:
        options:
          model: gpt-4o-mini

log-insight:
  enabled: true
  ai:
    provider: openai               # solo para trazabilidad en logs
    language: Spanish              # idioma de las respuestas de análisis
```

### 3. Listo

```
http://localhost:8080/log-insight
```

Los errores de tu aplicación se analizan automáticamente y aparecen en el dashboard.

---

## Configuración completa

```yaml
# La API key y el modelo se configuran bajo spring.ai, no bajo log-insight.
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini

log-insight:
  enabled: true

  ai:
    provider: openai          # openai | anthropic | ollama (solo trazabilidad)
    language: Spanish         # idioma de las respuestas de análisis

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

Debes declarar **exactamente uno** de estos starters en tu proyecto. Log Insight usa la abstracción `ChatClient` de Spring AI, pero el bean que la implementa lo registra el starter del provider concreto.

| Provider | Starter Maven/Gradle | Versión |
|----------|---------------------|---------|
| OpenAI | `spring-ai-starter-model-openai` | 1.0.0 |
| Anthropic (Claude) | `spring-ai-starter-model-anthropic` | 1.0.0 |
| Ollama (local/gratis) | `spring-ai-starter-model-ollama` | 1.0.0 |
| Azure OpenAI | `spring-ai-starter-model-azure-openai` | 1.0.0 |

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

## Solución de problemas

### `OpenAI API key must be set. Use the connection property: spring.ai.openai.api-key`

**Causa:** La propiedad `log-insight.ai.api-key` no existe ni es leída por Spring AI. La API key debe configurarse bajo las propiedades nativas de Spring AI, que son las que usa la autoconfiguración del proveedor.

**Solución:** Agrega la clave en `application.yml` bajo `spring.ai`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
```

Para otros proveedores: `spring.ai.anthropic.api-key`, `spring.ai.ollama.base-url`, etc.

---

### `Parameter 0 of method aiAnalysisService required a bean of type 'ChatClient$Builder' that could not be found`

**Causa:** Falta el starter del proveedor de IA en el classpath del proyecto que consume la librería. Log Insight incluye únicamente las abstracciones de Spring AI (`spring-ai-client-chat`), pero el bean `ChatClient.Builder` lo registra la autoconfiguración del provider concreto.

**Solución:** Agrega el starter correspondiente a tu `pom.xml` o `build.gradle`. Ejemplo con OpenAI:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>1.0.0</version>
</dependency>
```

Ver la [tabla de providers](#providers-de-ia-soportados) para otros proveedores. La configuración en `application.yml` (api-key, model, etc.) **no es suficiente** sin esta dependencia en el classpath.

### El dashboard `/log-insight` devuelve 404

**Causa más común:** Si tu proyecto tiene Spring Security, todas las rutas están protegidas por defecto, incluidas las de Log Insight.

**Solución:** Permite el acceso a las rutas del dashboard en tu configuración de seguridad:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/log-insight/**").permitAll()
        .anyRequest().authenticated()
    );
    return http.build();
}
```

O si usas configuración por propiedades:

```yaml
spring:
  security:
    filter:
      order: 10
```

> **Nota:** Exponer `/log-insight` sin autenticación muestra logs internos de tu aplicación. Evalúa restringir el acceso solo a redes internas o roles administrativos en entornos de producción.

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

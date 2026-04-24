# Log Insight — Spring Boot Starter
> Librería open source para análisis inteligente de logs en microservicios Spring Boot usando IA.

---

## Visión general

Una librería que intercepta errores de logs en aplicaciones Spring Boot, los sanitiza, y los envía a un modelo de IA (vía Spring AI) para obtener análisis, causa raíz y sugerencias en lenguaje natural. Incluye un dashboard web integrado accesible desde el propio microservicio.

---

## Decisiones de diseño confirmadas

### Distribución
- Publicada en **Maven Central** vía Sonatype OSSRH (gratuito)
- `groupId`: `io.github.tuusuario` (no requiere dominio propio)
- **Licencia**: Apache 2.0 (compatible con el ecosistema Spring, permite uso comercial)
- Open source en GitHub

### Integración de IA
- Usa **Spring AI** como capa de abstracción
- El usuario configura el provider en `application.yml`: `openai`, `anthropic`, `ollama`
- El usuario elige el modelo (impacta costo directamente)

### Sanitización (antes de enviar a la IA)
- **Obligatoria y configurable**
- Built-in: emails, credit cards, JWT tokens, IPs (activables/desactivables individualmente)
- Custom: patrones regex definidos por el usuario con su propio `replacement`
- Es el módulo más sensible — protegido con `CODEOWNERS`

### Deduplicación
- Compartida entre instancias vía **Redis** (desde v1)
- Interfaz interna `DeduplicationStore` para facilitar futuras implementaciones
- Ventana de tiempo configurable (default: 10 min)

### Control de costos
- Rate limit configurable (max por minuto y por hora)
- Máximo de líneas de stack trace enviadas a la IA
- Líneas de contexto previas al error (configurable)
- Solo niveles `ERROR` y `WARN` por defecto (configurable)
- Deduplicación Redis evita análisis repetidos entre instancias

### Salida por defecto
- **Consola** (opción A): si el usuario no configura nada, el análisis se imprime en el log
- Desactivable con `log-insight.default-output: none`

### Sinks built-in
| Sink | Descripción |
|------|-------------|
| `console` | Imprime análisis en el log (default: enabled) |
| `webhook` | HTTP POST a cualquier URL con headers configurables |
| `slack` | Webhook de Slack con canal configurable |
| `actuator` | Endpoint `/actuator/log-insight` con historial en memoria |

### Extensibilidad custom
- `@EventListener` sobre `LogInsightEvent` para cualquier lógica custom
- El evento expone: mensaje original sanitizado, análisis, causa raíz, sugerencias, severidad, nombre del servicio

### Dashboard Web (UI integrada)
- Patrón idéntico a Swagger UI / H2 Console / Eureka
- Disponible automáticamente en `http://localhost:8080/log-insight`
- Ruta configurable con `log-insight.ui.path`
- **Stack**: HTML + CSS + JS puro (sin dependencias extra para el usuario)
- API REST interna que el JS consume

#### Diseño UI/UX
- **Dark theme por defecto** (desarrolladores de monitoreo)
- Inspiración visual: Grafana + Vercel Dashboard + Linear
- Fuentes: `Inter` para UI general, `JetBrains Mono` para logs/stack traces
- Paleta de colores:
  - Fondo: `#0f1117`
  - Superficie: `#1a1d27`
  - CRITICAL: `#ff4757`
  - HIGH: `#ff6b35`
  - MEDIUM: `#ffa502`
  - LOW: `#2ed573`
  - Brand/acento: `#6c63ff`
- Animaciones sutiles y funcionales, no decorativas

#### Vistas del dashboard
1. **Dashboard principal**: contadores por severidad, gráfico de actividad 24h, lista de análisis recientes con búsqueda y filtros
2. **Vista detalle**: análisis completo, sugerencias numeradas, log original sanitizado, ocurrencias

---

## Configuración completa de referencia

```yaml
log-insight:
  enabled: true

  ai:
    provider: openai          # openai | anthropic | ollama
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini

  capture:
    levels: [ERROR, WARN]
    max-stack-lines: 30
    context-lines: 10

  sanitization:
    enabled: true
    built-in:
      emails: true
      credit-cards: true
      jwt-tokens: true
      ip-addresses: false
      uuids: false
    custom:
      - name: "internal-user-id"
        pattern: "userId=\\d+"
        replacement: "userId=[REDACTED]"

  deduplication:
    enabled: true
    window: 10m

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

## Estructura de módulos Maven

```
log-insight/
├── log-insight-core/               # lógica pura sin Spring (modelos, interfaces, pipeline)
├── log-insight-autoconfigure/      # auto-configuración Spring Boot, appender, Spring AI
├── log-insight-starter/            # wrapper thin (solo el pom, agrega las deps)
├── log-insight-ui/                 # recursos estáticos: HTML, CSS, JS del dashboard
├── log-insight-redis/              # módulo Redis opcional para deduplicación compartida
├── log-insight-sinks/
│   ├── log-insight-sink-slack/
│   └── log-insight-sink-webhook/
└── log-insight-sample/             # app Spring Boot de ejemplo y playground para contribuidores
```

---

## Flujo interno

```
Error en la app
      ↓
Logback/Log4j2 Appender captura
      ↓
Sanitización (built-in + reglas custom)
      ↓
Deduplicación vía Redis → ¿visto en ventana de tiempo? → descartar
      ↓
Rate Limiter → ¿excede cuota? → descartar
      ↓
Cola asíncrona (no bloquea el hilo principal)
      ↓
Spring AI → prompt con contexto del error
      ↓
         ┌──────────────────────────────┐
         │                              │
   LogInsightEvent               Persistencia en memoria
   @EventListener (custom)       + Redis (entre instancias)
                                        │
                                  REST API interna
                                        │
                                  Web UI /log-insight
```

---

## Seguridad del proyecto open source

### Flujo de contribución
- Nadie pushea directo a `main` — todo entra por Pull Request
- Mínimo 1 revisión aprobada antes de mergear
- CI debe pasar obligatoriamente

### GitHub Actions en cada PR
- Build + tests
- Análisis estático con **CodeQL** (gratis, mantenido por GitHub)
- Scan de dependencias con **Dependabot**
- Cobertura mínima de tests

### CODEOWNERS
- Módulo de sanitización protegido — requiere aprobación del mantenedor principal
- Módulo de autoconfigure protegido

### Buenas prácticas para el mantenedor
- No mergear PRs sin leer el código aunque CI pase
- No dar acceso de escritura a desconocidos
- No ejecutar scripts de build de PRs sin revisarlos

---

## Roadmap de desarrollo

### v1.0.0 — MVP
- [x] Estructura Maven multi-módulo
- [x] `log-insight-core`: modelos, interfaces, sanitización, deduplicación en memoria, tests
- [x] `log-insight-autoconfigure`: appender, Spring AI, pipeline asíncrono, rate limiter, auto-config
- [x] `log-insight-ui`: dashboard web (dark theme, HTML/CSS/JS vanilla, REST API interna)
- [x] `log-insight-redis`: deduplicación compartida entre instancias, failsafe si Redis no responde
- [x] Sink console (default)
- [x] `log-insight-sample`: app de demo con 5 endpoints de error y application.yml documentado
- [x] Contexto HTTP en dashboard (método + path del request que causó el error)
- [x] Idioma configurable para respuestas de IA (`log-insight.ai.language`)
- [x] **Prueba end-to-end exitosa** — dashboard, análisis IA, sanitización y Redis funcionando
- [ ] Publicación en Maven Central
- [x] README con arquitectura, tecnologías y quickstart
- [x] CONTRIBUTING.md
- [x] GitHub Actions CI/CD (build + CodeQL)

### v1.1.0
- [ ] Sink Slack built-in
- [ ] Sink Webhook built-in
- [ ] Actuator endpoint

### v2.0.0
- [ ] Soporte multi-servicio (agregar nombre del servicio como dimensión en UI)
- [ ] Prompt personalizable por el usuario
- [ ] Sink a base de datos (historial persistente)
- [ ] Métricas Micrometer (errores analizados, costo estimado de tokens)
```

---

## Orden de implementación

1. Estructura del proyecto Maven multi-módulo
2. `log-insight-core` — modelos, interfaces, pipeline
3. `log-insight-autoconfigure` — configuración, appender, Spring AI
4. `log-insight-ui` — dashboard web
5. `log-insight-redis` — deduplicación compartida
6. `log-insight-sample` — app de demo

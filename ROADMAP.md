# Roadmap — Log Insight

Este documento describe el estado actual y la dirección futura del proyecto. Si alguno de estos ítems te interesa, abre un Issue o un PR — las contribuciones son bienvenidas.

---

## v1.0.0 — MVP (publicado)

La base funcional de la librería.

- [x] Captura de logs `ERROR` y `WARN` vía Logback appender
- [x] Sanitización configurable: emails, tarjetas de crédito, JWT, IPs, UUIDs y patrones custom
- [x] Análisis con IA vía Spring AI (OpenAI, Anthropic, Ollama)
- [x] Idioma configurable para respuestas de IA (`log-insight.ai.language`)
- [x] Deduplicación en memoria y compartida entre instancias vía Redis
- [x] Rate limiting configurable por minuto y por hora
- [x] Dashboard web integrado con dark theme (HTML/CSS/JS puro, sin dependencias extra)
- [x] Contexto HTTP en el dashboard (método + path del request que generó el error)
- [x] Sink `console` habilitado por defecto
- [x] App de demo (`log-insight-sample`) con endpoints de error y configuración documentada
- [x] GitHub Actions CI/CD (build, tests, CodeQL)
- [x] Publicación en Maven Central

---

## v1.1.0 — Sinks adicionales

Más formas de recibir los análisis donde ya trabajas.

- [ ] **Sink Slack** — envío a canal con webhook configurable
- [ ] **Sink Webhook** — HTTP POST a cualquier URL con headers personalizados
- [ ] **Actuator endpoint** — `/actuator/log-insight` con historial de análisis

¿Quieres implementar uno de estos? Consulta [CONTRIBUTING.md](CONTRIBUTING.md).

---

## v2.0.0 — Madurez y personalización

- [ ] **Prompt personalizable** — define tu propio template para el análisis de IA
- [ ] **Soporte multi-servicio** — visualiza análisis de varios microservicios en un solo dashboard
- [ ] **Persistencia en base de datos** — historial de análisis más allá de la memoria de la JVM
- [ ] **Métricas Micrometer** — errores analizados, tokens consumidos, costo estimado

---

## Ideas y propuestas

¿Tienes una idea que no aparece aquí? Abre un [Issue](https://github.com/mvillasono/log-insight/issues) con el label `enhancement` y lo discutimos.

Algunas ideas recibidas de la comunidad que están siendo evaluadas:

- Sink para Microsoft Teams
- Sink para PagerDuty
- Reglas de sanitización adicionales built-in (SSNs, IBAN, tokens OAuth2)
- Soporte para Log4j2 appender

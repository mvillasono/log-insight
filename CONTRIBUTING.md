# Contribuir a Log Insight

¡Gracias por tu interés en mejorar Log Insight! Esta guía te explica cómo empezar.

---

## Configurar el entorno local

**Requisitos:**
- Java 21
- Maven 3.9+
- Docker (para levantar Redis en tests de integración)

```bash
git clone https://github.com/mvillasono/log-insight
cd log-insight
mvn install -DskipTests

# Correr el sample para ver el dashboard
cd log-insight-sample
mvn spring-boot:run
# → http://localhost:8080/log-insight
```

---

## Flujo para contribuir

1. Haz **fork** del repositorio
2. Crea una rama descriptiva: `git checkout -b feature/sink-teams`
3. Escribe tu código y sus tests
4. Abre un **Pull Request** hacia `main`
5. Espera la revisión — CI debe pasar antes del merge

> Nadie puede hacer push directo a `main`, incluyendo los mantenedores.

---

## Estructura de módulos

| Módulo | Qué toca |
|--------|---------|
| `log-insight-core` | Modelos e interfaces puras — sin Spring |
| `log-insight-autoconfigure` | Auto-config, appender, Spring AI |
| `log-insight-ui` | Dashboard HTML/CSS/JS |
| `log-insight-redis` | Deduplicación Redis |
| `log-insight-sink-*` | Nuevos sinks built-in |
| `log-insight-sample` | App de demo |

Cada módulo es independiente. Puedes contribuir a uno sin tocar los demás.

---

## Reglas importantes

- **Sanitización y autoconfigure** son áreas protegidas (CODEOWNERS) — cambios ahí requieren revisión del mantenedor principal
- Los tests deben pasar (`mvn verify`)
- No incluyas API keys, `.env` ni secretos en el código
- Mantén la cobertura de tests existente

---

## Tipos de contribución bienvenidos

- Nuevos sinks (Teams, PagerDuty, email)
- Nuevas reglas de sanitización built-in
- Mejoras al dashboard UI/UX
- Tests y cobertura
- Documentación y ejemplos en otros idiomas

---

## Reportar bugs

Abre un [Issue](https://github.com/mvillasono/log-insight/issues) con:
- Versión de Log Insight
- Versión de Spring Boot y Java
- Configuración relevante (sin secretos)
- Stack trace o comportamiento esperado vs. real

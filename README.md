# Red Vecinal IA

> Asistente conversacional de IA **100% offline** para vecinos de Villa El Salvador, funcionando sobre una red WiFi comunitaria desplegada con equipamiento Mikrotik.

<p align="center">
  <strong>Hackatón HackODS UA 2026 — Semana de la IA</strong><br>
  Universidad Autónoma del Perú · 23 de noviembre de 2026
</p>

---

## 🎯 Alineación ODS

Este proyecto aborda un desafío combinado de los Objetivos de Desarrollo Sostenible:

| ODS | Eje | Cómo lo cubrimos |
|---|---|---|
| **ODS 10** — Reducción de desigualdades | Inclusión digital (eje primario) | Lleva conectividad + un asistente de IA a una AAHH de Villa El Salvador donde el acceso a información sobre trámites, salud, empleo y denuncia es limitado |
| **ODS 5** — Igualdad de género | Prevención de violencia (eje secundario) | El módulo de denuncia ciudadana del asistente canaliza reportes de alumbrado malogrado, robos y otros incidentes que afectan desproporcionadamente a mujeres en espacios públicos |

---

## 📍 Contexto

**Zona objetivo:** AAHH Unión, Villa El Salvador (Lima Sur, Perú).

Los vecinos de la AAHH Unión enfrentan tres problemas conectados:

1. **Conectividad limitada:** muchos hogares no tienen internet de calidad o este es caro.
2. **Barrera de información:** los trámites municipales, los servicios de salud, los programas de empleo y los canales de denuncia son confusos, dispersos o desconocidos.
3. **Desconfianza en la denuncia:** la falta de un canal accesible y discreto reduce el reporte de incidentes que afectan la seguridad del barrio.

**Red Vecinal IA** resuelve los tres con un solo despliegue: una red WiFi comunitaria gratuita que al conectarse abre automáticamente un asistente de IA local (sin internet externo) que responde preguntas de trámites, salud, empleo y denuncia en español peruano, con datos verificados de las instituciones reales de VES.

---

## ✨ Características

- 🤖 **IA local con Ollama qwen2.5:7b** — 100% offline, sin dependencia de internet del venue ni de APIs externas de pago
- ⚡ **Cache de FAQs de dos niveles** — normalización + distancia de Levenshtein ≤ 3; respuestas frecuentes en <50 ms sin tocar el modelo
- 📚 **10 FAQs verificadas** precargadas con datos reales de Municipalidad de VES, SUNAT, Sedapal, SAMU, PNP y programas del MTPE
- 📋 **5 trámites detallados** con pasos numerados, descripción y URL oficial
- 💬 **Persistencia de conversaciones** — cada intercambio se guarda en SQLite para auditoría y mejora futura
- 🌐 **4 endpoints REST** documentados y probados con curl
- 🔧 **Despliegue con hardware Mikrotik** — RB750Gr3 + TP-Link en modo bridge + hotspot con walled garden

---

## 🏗️ Arquitectura

```
                    ┌─────────────────────────────────┐
                    │   Celular del vecino (cliente)  │
                    └──────────────┬──────────────────┘
                                   │ WiFi "RedVecinalIA"
                                   ▼
                    ┌─────────────────────────────────┐
                    │   TP-Link (modo bridge/AP)      │
                    └──────────────┬──────────────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────────┐
                    │   Mikrotik RB750Gr3 (hEX S)     │
                    │   · DHCP 192.168.88.0/24        │
                    │   · NAT hacia WAN               │
                    │   · Hotspot + walled garden     │
                    │   · Portal cautivo → :8080      │
                    └──────────────┬──────────────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────────┐
                    │   Laptop servidor (i7, 32 GB)   │
                    │  ┌───────────────────────────┐  │
                    │  │  Spring Boot 4.1.2 :8080  │  │
                    │  │  ├─ /api/health           │  │
                    │  │  ├─ /api/faqs             │  │
                    │  │  ├─ /api/tramites         │  │
                    │  │  └─ /api/chat ← POST      │  │
                    │  │       │                   │  │
                    │  │       ├─ FAQ cache (<50ms)│  │
                    │  │       │   ↓ miss           │  │
                    │  │       └─ OllamaService    │  │
                    │  └───────────┬───────────────┘  │
                    │              │                  │
                    │  ┌───────────▼───────────────┐  │
                    │  │  SQLite (red_vecinal.db)  │  │
                    │  │  · conversaciones         │  │
                    │  │  · faqs (10)              │  │
                    │  │  · mensajes               │  │
                    │  │  · tramites (5)           │  │
                    │  └───────────────────────────┘  │
                    │              │                  │
                    │  ┌───────────▼───────────────┐  │
                    │  │  Ollama qwen2.5:7b :11434 │  │
                    │  │  (modelo IA local)        │  │
                    │  └───────────────────────────┘  │
                    └─────────────────────────────────┘
```

---

## 🛠️ Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Backend | Spring Boot | 4.1.2 |
| Lenguaje | Java | 17 LTS (Eclipse Temurin 17.0.21+9) |
| Base de datos | SQLite | 3 (vía sqlite-jdbc) |
| ORM | Hibernate | (incluido en Spring Boot 4.1.2) |
| IA local | Ollama | 0.40.16 |
| Modelo IA | qwen2.5:7b | 4.7 GB descarga, ~7 GB RAM runtime |
| Router | Mikrotik RouterOS | 7 (RB750Gr3 hEX S) |
| Frontend (próximo) | PWA HTML + CSS + JS vanilla | sin framework |

---

## 📡 API REST

### `GET /api/health`
Health check con alineación ODS.

```bash
curl http://localhost:8080/api/health
```
```json
{
  "status": "ok",
  "service": "Red Vecinal IA",
  "version": "0.1.0",
  "ods": ["ODS 10 - Inclusión digital", "ODS 5 - Prevención de violencia"]
}
```

### `GET /api/faqs`
Lista las 10 FAQs precargadas (5 categorías: Tramite, Salud, Empleo, Denuncia, Servicios).

```bash
curl http://localhost:8080/api/faqs
```

### `GET /api/tramites`
Lista los 5 trámites con pasos detallados y URL oficial.

```bash
curl http://localhost:8080/api/tramites
```

### `POST /api/chat`
Endpoint principal del asistente. Recibe `{mensaje, categoria, sessionId}` y devuelve `{respuesta, sessionId, tiempoMs, fuente}`.

**Pregunta cacheada (responde instantáneo desde FAQ):**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"mensaje":"donde pago el impuesto predial","categoria":"Tramite"}'
```
```json
{
  "respuesta": "En la caja de la Municipalidad de VES o en agencias del Banco de la Nacion...",
  "sessionId": "6187d290-948c-49fe-aa53-78f765a0e7b2",
  "tiempoMs": 1831,
  "fuente": "faq"
}
```

**Pregunta nueva (cae a Ollama IA):**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"mensaje":"que horario tiene la municipalidad los sabados","categoria":"Tramite"}'
```
```json
{
  "respuesta": "El horario de atención de la municipalidad los sábados generalmente es de 07:30 a 12:30 horas...",
  "sessionId": "97360c70-70d3-4725-90c8-f21a48a991dd",
  "tiempoMs": 36518,
  "fuente": "ollama"
}
```

> El campo `fuente` indica si la respuesta vino del cache FAQ (`"faq"`, <50 ms) o del modelo IA (`"ollama"`, ~36 s en CPU).

---

## 📊 Datos precargados

### 10 FAQs (5 categorías)

| # | Categoría | Pregunta |
|---|---|---|
| 1 | Tramite | ¿Cómo saco mi certificado de obra en la municipalidad de VES? |
| 2 | Tramite | ¿Dónde pago el impuesto predial? |
| 3 | Salud | ¿Dónde está el centro de salud más cercano a La Unión? |
| 4 | Salud | Mi hijo tiene fiebre alta, ¿qué centro atiende de noche? |
| 5 | Empleo | ¿Cómo formalizo mi negocio de comida en el parque? |
| 6 | Empleo | ¿Dónde hay cursos técnicos gratuitos en VES? |
| 7 | Empleo | ¿Cómo obtengo RUC? |
| 8 | Denuncia | Quiero reportar alumbrado público malogrado |
| 9 | Denuncia | Vi robos cerca del mercado, ¿a quién llamo? |
| 10 | Servicios | No tengo agua hace 3 días, ¿cómo reporto a Sedapal? |

### 5 trámites con pasos detallados

1. **Certificado de obra** → munives.gob.pe
2. **Impuesto predial** → munives.gob.pe
3. **Formalización de negocio de alimentos** → sunat.gob.pe
4. **Inscripción al RUC** → sunat.gob.pe
5. **Cursos técnicos gratuitos (CETPRO / Jóvenes a la Obra)** → plataformas.empleo.gob.pe

---

## 📁 Estructura del proyecto

```
redvecinal-ia/
├── src/main/java/com/redvecinalia/
│   ├── RedvecinalIaApplication.java          # Clase principal
│   ├── controller/
│   │   ├── HealthController.java             # GET /api/health
│   │   ├── TramiteController.java            # GET /api/tramites
│   │   ├── FAQController.java                # GET /api/faqs
│   │   └── ChatController.java               # POST /api/chat
│   ├── model/
│   │   ├── Conversacion.java                 # @Table("conversaciones")
│   │   ├── FAQ.java                          # @Table("faqs")
│   │   ├── Mensaje.java                      # @Table("mensajes")
│   │   └── Tramite.java                      # @Table("tramites")
│   ├── repository/
│   │   ├── TramiteRepository.java
│   │   ├── FAQRepository.java
│   │   ├── ConversacionRepository.java       # findBySessionId()
│   │   └── MensajeRepository.java
│   ├── dto/
│   │   ├── ChatRequest.java                  # {mensaje, categoria, sessionId}
│   │   └── ChatResponse.java                 # {respuesta, sessionId, tiempoMs, fuente}
│   ├── service/
│   │   ├── OllamaService.java                # RestClient → localhost:11434
│   │   ├── CacheService.java                 # normalizar() + levenshtein()
│   │   └── ChatService.java                  # orquestación FAQ → Ollama
│   └── runner/
│       └── DataInitializer.java              # Carga 10 FAQs + 5 trámites al arranque
├── src/main/resources/
│   └── application.properties
├── pom.xml
└── red_vecinal.db                             # SQLite (se crea automáticamente)
```

---

## 🚀 Instalación y ejecución

### Prerrequisitos

1. **Java 17 LTS** (Eclipse Temurin recomendado)
   ```powershell
   java -version
   # openjdk version "17.0.21" o superior
   ```

2. **Ollama** con el modelo `qwen2.5:7b`
   ```powershell
   # Instalar Ollama desde https://ollama.com
   ollama pull qwen2.5:7b
   ollama serve   # mantener corriendo en otra terminal
   curl http://localhost:11434/api/tags   # verificar
   ```

3. **Maven** (incluido vía `mvnw.cmd` / `mvnw` en el repo)

### Pasos

```bash
# 1. Clonar el repo
git clone https://github.com/<usuario>/redvecinal-ia.git
cd redvecinal-ia

# 2. Compilar y arrancar
./mvnw.cmd clean spring-boot:run    # Windows
./mvnw clean spring-boot:run        # Linux/Mac

# 3. Verificar
curl http://localhost:8080/api/health
```

El `DataInitializer` carga automáticamente las 10 FAQs y 5 trámites al primer arranque. La base de datos `red_vecinal.db` se crea en la raíz del proyecto.

---

## 🔧 Configuración

`src/main/resources/application.properties`:

```properties
server.port=8080
spring.datasource.url=jdbc:sqlite:red_vecinal.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.web.resources.static-locations=classpath:/static/
ollama.url=http://localhost:11434
ollama.model=qwen2.5:7b
```

---

## 🧠 Cómo funciona el cache de FAQs

El `ChatService.procesar()` sigue este flujo para cada mensaje entrante:

```
mensaje entrante
    │
    ▼
normalizar (minúsculas, sin signos)
    │
    ▼
buscar en 10 FAQs:
    · coincidencia exacta normalizada, OR
    · distancia Levenshtein ≤ 3
    │
    ├─ HIT → responder desde SQLite
    │        fuente: "faq", tiempoMs: <50
    │
    └─ MISS → llamar OllamaService.generate()
              con system prompt + mensaje
              fuente: "ollama", tiempoMs: ~36000
    │
    ▼
persistir mensaje + respuesta en Conversacion/Mensaje
devolver ChatResponse
```

**System prompt** (en `ChatService.SYSTEM_PROMPT`):
> Eres el Asistente de la Red Vecinal IA, un servicio gratuito para los vecinos de Villa El Salvador, especialmente la AAHH Unión. Respondes en español peruano, en tono cercano y respetuoso. Tu alcance es: trámites municipales, denuncia ciudadana, salud y empleabilidad. Tus respuestas son breves (máximo 200 palabras), concretas y accionables. Nunca inventas datos.

---

## 👥 Equipo

| Integrante | Rol | Perfil |
|---|---|---|
| **Napoleón Jesús Rodríguez La Torre** | Backend + redes Mikrotik | Ing. Sistemas UAP Lima Norte · Consultor Mikrotik certificado (5 certificaciones internacionales) · Fundador de academia de telecomunicaciones desde 2008 (+1000 alumnos, cliente FF.AA. del Perú) |
| **Frank Nilser Simpertigue** | Co-equipamiento | Co-responsable de inscripción |

**Profesor referente:** Pedro José Angulo Landaeta

---

## 🎤 Contexto del evento

- **Evento:** Hackatón HackODS UA 2026 — Semana de la IA
- **Fecha:** Lunes 23 de noviembre de 2026, 12:00 pm - 7:00 pm
- **Lugar:** Auditorio principal, Universidad Autónoma del Perú
- **Modalidad:** Prototipo funcional con demo en vivo (no mockup)
- **Duración del pitch:** 3 minutos, máximo 5 diapositivas

### Criterios de evaluación
- Impacto: 30%
- Innovación: 25%
- Ejecución técnica: 30%
- Presentación (pitch): 15%

---

## 📈 Roadmap

- [x] Backend Spring Boot 4.1.2 con 4 endpoints REST
- [x] Integración Ollama qwen2.5:7b local
- [x] Cache de FAQs con Levenshtein
- [x] 10 FAQs + 5 trámites precargados con datos reales de VES
- [ ] Frontend PWA (HTML + CSS + JS vanilla)
- [ ] Portal cautivo HTML para Mikrotik Hotspot
- [ ] Configuración Mikrotik RB750Gr3 (bridge + DHCP + NAT + Hotspot + walled garden)
- [ ] Video de demo backup
- [ ] Pitch de 3 minutos ensayado

---

## 📄 Licencia

Proyecto desarrollado para el Hackatón HackODS UA 2026. Uso educativo y de prototipo.

---

## 🙏 Reconocimientos

- Municipalidad de Villa El Salvador (munives.gob.pe)
- SUNAT
- Sedapal
- SAMU (106) · PNP (105)
- Programa Jóvenes a la Obra del MTPE
- CETPRO VES

Los datos de trámites, teléfonos y URLs utilizados en las FAQs y trámites son públicos y verificables en los portales oficiales de cada institución.

# Red Vecinal IA

> Red comunitaria libre y offline con asistente IA local para Villa El Salvador.
> Proyecto para **HackODS UA 2026** — Universidad Autónoma del Perú.

![ODS 10](https://img.shields.io/badge/ODS-10-Inclusión%20digital-blue)
![ODS 5](https://img.shields.io/badge/ODS-5-Prevención%20violencia-orange)
![Spring Boot 4.1.2](https://img.shields.io/badge/Spring%20Boot-4.1.2-green)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Ollama](https://img.shields.io/badge/IA-Ollama%20qwen2.5%3A7b-purple)

---

## El problema

En Villa El Salvador, especialmente la **AAHH Unión**, los vecinos enfrentan:

- **Brecha digital**: muchos no tienen acceso a internet ni datos móviles
- **Violencia familiar**: uno de los delitos más denunciados según INEI 2024
- **Falta de información**: aunque existan servicios (comisaría, Sedapal, municipalidad), los vecinos no saben dónde acudir ni cómo iniciar trámites

Cuando un vecino necesita ayuda urgente y no tiene internet, no encuentra la información. **La víctima conoce el dolor, pero no la ruta. Sin información, la violencia continúa.**

---

## La solución

**Red Vecinal IA** es una red WiFi libre y offline con un asistente de inteligencia artificial local que:

- Funciona **100% sin internet externo**
- Lleva **información verificada** al barrio (no inventada)
- Cubre **trámites, denuncia, salud, empleo y servicios públicos**
- Toda la IA corre en una laptop dentro del nodo comunitario

### Flujo del vecino

```
Vecino se conecta al WiFi "Red Vecinal IA" (gratuito, sin contraseña)
        ↓
Portal cautivo aparece automáticamente en el navegador
        ↓
Clic en "Conectar al asistente"
        ↓
Se abre el chat web (PWA)
        ↓
El vecino hace su pregunta
        ↓
El asistente responde con datos verificados:
  - Trámites municipales (impuesto predial, certificados, licencias)
  - Denuncia ciudadana (violencia familiar, robos, alumbrado)
  - Salud (centros cercanos, SAMU 106)
  - Empleo (RUC, formalización, cursos técnicos)
  - Servicios (Sedapal 119)
```

**Toda esta experiencia funciona sin un solo byte de internet externo.**

---

## Arquitectura

### Hardware

| Componente | Modelo | Rol |
|---|---|---|
| Router + WiFi AP | **Mikrotik LDF-5nD** (RouterOS 6.49.18) | Gateway, DHCP, Hotspot, walled garden, WiFi 5GHz |
| Servidor | Laptop i7-1065G7 10ª gen, 32 GB RAM, NVIDIA GeForce | Spring Boot + Ollama + SQLite |
| Cableado | 1 cable Ethernet (LDF5 → laptop) | Conexión servidor al bridge |

### Software

| Capa | Tecnología | Versión |
|---|---|---|
| Backend | Spring Boot | 4.1.2 |
| Lenguaje | Java LTS | 17 (Temurin 17.0.21+9) |
| BD | SQLite | 3.46.1 |
| IA local | Ollama + qwen2.5:7b | 100% offline |
| Frontend | PWA HTML+CSS+JS vanilla | Sin framework |
| Router OS | Mikrotik RouterOS | 6.49.18 |

### Topología de red

```
                    [LDF-5nD Mikrotik]
                    ├─ wlan1 (WiFi 5GHz "Red Vecinal IA")
                    └─ ether1 ← cable → [Laptop servidor]
                                          IP: 192.168.88.10
                    IP del Mikrotik: 192.168.88.1
                    DNS: redvecinal.local
                    DHCP pool: 192.168.88.20-200
                    Hotspot: portal cautivo + walled garden
```

---

## ODS alineados

| ODS | Eje | Cómo lo tocamos |
|---|---|---|
| **ODS 10 — Reducción de desigualdades** | Primario | Llevamos conectividad e IA donde ni Google ni el Estado llegan |
| **ODS 5 — Igualdad de género** | Secundario | Módulo de denuncia acorta tiempo entre incidente y respuesta institucional |

---

## Endpoints REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/health` | Health check con ODS alineados |
| GET | `/api/faqs` | Lista de FAQs cacheadas (11 entradas verificadas) |
| GET | `/api/tramites` | Lista de trámites precargados (5 entradas) |
| POST | `/api/chat` | Chat con asistente (FAQ cacheada + Ollama fallback) |

---

## Cómo ejecutarlo

### Requisitos

- Java 17 JDK (Temurin recomendado: https://adoptium.net)
- Ollama instalado con modelo `qwen2.5:7b`
- (Opcional) Mikrotik LDF-5nD con hotspot configurado para demo completa

### Pasos

1. **Clonar el repo:**

```powershell
git clone https://github.com/tumaster-sistemas/redvecinal-ia.git
cd redvecinal-ia
```

2. **Iniciar Ollama (servicio en background):**

```powershell
ollama serve
```

3. **Verificar que Ollama responde:**

```powershell
curl http://localhost:11434/api/tags
```

Debe devolver JSON con los modelos instalados.

4. **Iniciar Spring Boot:**

```powershell
.\mvnw.cmd spring-boot:run
```

5. **Abrir el asistente en el navegador:**

```
http://localhost:8080
```

---

## Casos de uso

### Pregunta cacheada (respuesta instantánea <2s)

```powershell
$body = @{mensaje="donde pago el impuesto predial";categoria="Tramite"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method Post -ContentType "application/json" -Body $body | ConvertTo-Json
```

### Pregunta de violencia familiar (cacheada, instantánea)

```powershell
$body = @{mensaje="como denuncio violencia familiar";categoria="Denuncia"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method Post -ContentType "application/json" -Body $body | ConvertTo-Json
```

### Pregunta nueva (cae a IA local, ~30s)

```powershell
$body = @{mensaje="que horario tiene la municipalidad los sabados";categoria="Tramite"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method Post -ContentType "application/json" -Body $body | ConvertTo-Json
```

---

## Sistema anti-alucinación

El asistente tiene un system prompt estricto que:

1. **NUNCA inventa datos** — si no conoce un dato con certeza, no lo menciona
2. **Solo cita fuentes verificables** — teléfonos oficiales (105, 106, 100, 119, 165), URLs .gob.pe, direcciones reales de VES
3. **Si no sabe, ofrece alternativa real** — responde: *"No cuento con esa información registrada, pero te comparto esta alternativa que puede servirte:"*
4. **Solo Villa El Salvador** — nunca inventa direcciones de otros distritos
5. **Siempre termina con un teléfono o URL verificable**

---

## Equipo

### IncluyeVES

**Napoleón Rodríguez La Torre** — Líder + técnico + presentador
- Consultor internacional Mikrotik (5 certificaciones internacionales)
- Fundador de academia de telecomunicaciones desde 2008 (digitalizada 2014)
- +1000 alumnos formados, incluyendo Fuerzas Armadas del Perú
- Fabricante histórico de antenas outdoor 2.4/5.8 GHz (+6500 sectoriales vendidas)
- 13 seminarios online activos

**Frank Nilser Simpertigue** — Apoyo demo + Q&A

---

## Evento

- **HackODS UA 2026** — Universidad Autónoma del Perú
- 23 de noviembre de 2026, 12:00 pm - 7:00 pm
- Auditorio principal UAP
- Desafío combinado: **ODS 10 + ODS 5**
- Premios: 1° S/1,500 · 2° S/1,000 · 3° S/500

---

## Replicabilidad

Cada nodo comunitario cuesta aproximadamente **US$135**:

- Mikrotik LDF-5nD (o RB750Gr3 + AP externo): ~US$60-80
- Raspberry Pi 4 como servidor (alternativa a laptop): ~US$60-80
- Cableado y accesorios: ~US$10-15

Cada nodo atiende **50-100 familias**. Una asociación vecinal puede pagarlo.

---

## Licencia

MIT License — libre uso y modificación con atribución.

---

## Contacto

- **Repo:** https://github.com/tumaster-sistemas/redvecinal-ia
- **Evento:** hackods@ua.edu.pe
- **Universidad:** www.ua.edu.pe

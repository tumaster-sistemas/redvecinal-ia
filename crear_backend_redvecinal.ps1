<#
.SYNOPSIS
    Crea los 13 archivos Java restantes del backend Red Vecinal IA.
.DESCRIPTION
    Genera repositorios JPA, DTOs, controladores REST, servicios (Ollama + Chat + Cache)
    y el DataInitializer con los 10 casos reales de Villa El Salvador.
    Compatible con Spring Boot 4.1.2 + Java 17 + SQLite + Ollama qwen2.5:7b.
    Idempotente: sobrescribe archivos existentes sin preguntar.
.NOTES
    Ejecutar desde: D:\2027\hackaton\redvecinal-ia
    Autor: Asistente IA para Napoleón Rodríguez La Torre
    Fecha: 18/11/2026
#>

$ErrorActionPreference = "Stop"

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "pom.xml")) {
    Write-Host "ERROR: Ejecuta este script desde el directorio del proyecto (donde está pom.xml)" -ForegroundColor Red
    Write-Host "Directorio actual: $(Get-Location)" -ForegroundColor Yellow
    exit 1
}

$base = "src\main\java\com\redvecinalia"

# UTF-8 sin BOM (importante para que javac no falle)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

# Crear directorios necesarios
Write-Host "`n=== Creando estructura de paquetes ===" -ForegroundColor Cyan
foreach ($dir in @("repository", "service", "dto", "runner", "controller")) {
    $rutaDir = Join-Path $base $dir
    if (-not (Test-Path $rutaDir)) {
        New-Item -ItemType Directory -Path $rutaDir -Force | Out-Null
        Write-Host "  + $dir\" -ForegroundColor Yellow
    } else {
        Write-Host "  ✓ $dir\ (ya existe)" -ForegroundColor DarkGray
    }
}

# Función auxiliar para escribir archivos Java en UTF-8 sin BOM
function Escribir-Java {
    param([string]$RutaRelativa, [string]$Contenido)
    $rutaCompleta = Join-Path $base $RutaRelativa
    [System.IO.File]::WriteAllText($rutaCompleta, $Contenido, $utf8NoBom)
    Write-Host "  ✓ $RutaRelativa" -ForegroundColor Green
}

Write-Host "`n=== Creando 13 archivos Java ===" -ForegroundColor Cyan

# ============================================================
# 1/13 — TramiteRepository.java
# ============================================================
$contenido = @'
package com.redvecinalia.repository;

import com.redvecinalia.model.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
    List<Tramite> findByCategoriaOrderByTituloAsc(String categoria);
}
'@
Escribir-Java "repository\TramiteRepository.java" $contenido

# ============================================================
# 2/13 — FAQRepository.java
# ============================================================
$contenido = @'
package com.redvecinalia.repository;

import com.redvecinalia.model.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FAQRepository extends JpaRepository<FAQ, Long> {
    List<FAQ> findByCategoria(String categoria);
}
'@
Escribir-Java "repository\FAQRepository.java" $contenido

# ============================================================
# 3/13 — ConversacionRepository.java
# ============================================================
$contenido = @'
package com.redvecinalia.repository;

import com.redvecinalia.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversacionRepository extends JpaRepository<Conversacion, String> {
    Optional<Conversacion> findBySessionId(String sessionId);
}
'@
Escribir-Java "repository\ConversacionRepository.java" $contenido

# ============================================================
# 4/13 — MensajeRepository.java
# ============================================================
$contenido = @'
package com.redvecinalia.repository;

import com.redvecinalia.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByConversacionIdOrderByTimestampAsc(String conversacionId);
}
'@
Escribir-Java "repository\MensajeRepository.java" $contenido

# ============================================================
# 5/13 — ChatRequest.java (DTO)
# ============================================================
$contenido = @'
package com.redvecinalia.dto;

public class ChatRequest {

    private String mensaje;
    private String sessionId;
    private String categoria;

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
'@
Escribir-Java "dto\ChatRequest.java" $contenido

# ============================================================
# 6/13 — ChatResponse.java (DTO)
# ============================================================
$contenido = @'
package com.redvecinalia.dto;

public class ChatResponse {

    private String respuesta;
    private String sessionId;
    private long tiempoMs;
    private String fuente;

    public ChatResponse() {}

    public ChatResponse(String respuesta, String sessionId, long tiempoMs, String fuente) {
        this.respuesta = respuesta;
        this.sessionId = sessionId;
        this.tiempoMs = tiempoMs;
        this.fuente = fuente;
    }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public long getTiempoMs() { return tiempoMs; }
    public void setTiempoMs(long tiempoMs) { this.tiempoMs = tiempoMs; }

    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }
}
'@
Escribir-Java "dto\ChatResponse.java" $contenido

# ============================================================
# 7/13 — TramiteController.java
# ============================================================
$contenido = @'
package com.redvecinalia.controller;

import com.redvecinalia.model.Tramite;
import com.redvecinalia.repository.TramiteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tramites")
public class TramiteController {

    private final TramiteRepository tramiteRepository;

    public TramiteController(TramiteRepository tramiteRepository) {
        this.tramiteRepository = tramiteRepository;
    }

    @GetMapping
    public List<Tramite> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return tramiteRepository.findByCategoriaOrderByTituloAsc(categoria);
        }
        return tramiteRepository.findAll();
    }

    @GetMapping("/{id}")
    public Tramite obtener(@PathVariable Long id) {
        return tramiteRepository.findById(id).orElse(null);
    }
}
'@
Escribir-Java "controller\TramiteController.java" $contenido

# ============================================================
# 8/13 — FAQController.java
# ============================================================
$contenido = @'
package com.redvecinalia.controller;

import com.redvecinalia.model.FAQ;
import com.redvecinalia.repository.FAQRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FAQController {

    private final FAQRepository faqRepository;

    public FAQController(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @GetMapping
    public List<FAQ> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return faqRepository.findByCategoria(categoria);
        }
        return faqRepository.findAll();
    }
}
'@
Escribir-Java "controller\FAQController.java" $contenido

# ============================================================
# 9/13 — ChatController.java
# ============================================================
$contenido = @'
package com.redvecinalia.controller;

import com.redvecinalia.dto.ChatRequest;
import com.redvecinalia.dto.ChatResponse;
import com.redvecinalia.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request.getMensaje() == null || request.getMensaje().isBlank()) {
            return new ChatResponse(
                "Por favor escribe tu consulta.",
                request.getSessionId(),
                0,
                "error"
            );
        }
        return chatService.procesar(request);
    }
}
'@
Escribir-Java "controller\ChatController.java" $contenido

# ============================================================
# 10/13 — OllamaService.java
# ============================================================
$contenido = @'
package com.redvecinalia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class OllamaService {

    private final String ollamaUrl;
    private final String modelo;
    private final RestClient restClient;

    public OllamaService(
            @Value("${ollama.url:http://localhost:11434}") String ollamaUrl,
            @Value("${ollama.model:qwen2.5:7b}") String modelo) {
        this.ollamaUrl = ollamaUrl;
        this.modelo = modelo;
        this.restClient = RestClient.builder().baseUrl(ollamaUrl).build();
    }

    public String generate(String mensaje, String systemPrompt) {
        Map<String, Object> body = Map.of(
            "model", modelo,
            "prompt", mensaje,
            "system", systemPrompt,
            "stream", false
        );

        Map<String, Object> respuesta = restClient.post()
            .uri("/api/generate")
            .header("Content-Type", "application/json")
            .body(body)
            .retrieve()
            .body(Map.class);

        if (respuesta != null && respuesta.containsKey("response")) {
            return (String) respuesta.get("response");
        }
        return "No pude generar una respuesta en este momento. Puedes intentar reformular tu pregunta.";
    }

    public boolean isDisponible() {
        try {
            restClient.get()
                .uri("/api/tags")
                .retrieve()
                .body(Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getModelo() {
        return modelo;
    }
}
'@
Escribir-Java "service\OllamaService.java" $contenido

# ============================================================
# 11/13 — CacheService.java
# ============================================================
$contenido = @'
package com.redvecinalia.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
public class CacheService {

    public String normalizar(String texto) {
        if (texto == null) return "";
        String n = texto.toLowerCase();
        n = Normalizer.normalize(n, Normalizer.Form.NFD);
        n = n.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        n = n.replaceAll("[^a-z0-9\\s]", "");
        n = n.replaceAll("\\s+", " ").trim();
        return n;
    }

    public int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int costo = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + costo
                );
            }
        }
        return dp[a.length()][b.length()];
    }
}
'@
Escribir-Java "service\CacheService.java" $contenido

# ============================================================
# 12/13 — ChatService.java
# ============================================================
$contenido = @'
package com.redvecinalia.service;

import com.redvecinalia.dto.ChatRequest;
import com.redvecinalia.dto.ChatResponse;
import com.redvecinalia.model.Conversacion;
import com.redvecinalia.model.FAQ;
import com.redvecinalia.model.Mensaje;
import com.redvecinalia.repository.ConversacionRepository;
import com.redvecinalia.repository.FAQRepository;
import com.redvecinalia.repository.MensajeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
        Eres el Asistente de la Red Vecinal IA, un servicio gratuito
        para los vecinos de Villa El Salvador, especialmente la AAHH Union.
        Respondes en español peruano, en tono cercano y respetuoso.
        Tu alcance es: tramites municipales, denuncia ciudadana,
        salud y empleabilidad. Si te preguntan algo fuera de estos temas,
        respondes amablemente que solo puedes ayudar en esos cuatro temas.
        Tus respuestas son breves (maximo 200 palabras), concretas y
        accionables: si un vecino pregunta por un tramite, le dices donde,
        cuando y que documentos lleva. Si pide denunciar, le das el
        telefono o la direccion exacta. Nunca inventes datos.
        Si no sabes la respuesta, dices: "No tengo esa informacion
        registrada, pero puedes llamar al numero indicado o visitar
        el lugar correspondiente."
        """;

    private final FAQRepository faqRepository;
    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final OllamaService ollamaService;
    private final CacheService cacheService;

    public ChatService(FAQRepository faqRepository,
                       ConversacionRepository conversacionRepository,
                       MensajeRepository mensajeRepository,
                       OllamaService ollamaService,
                       CacheService cacheService) {
        this.faqRepository = faqRepository;
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.ollamaService = ollamaService;
        this.cacheService = cacheService;
    }

    public ChatResponse procesar(ChatRequest request) {
        long inicio = System.currentTimeMillis();

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        Conversacion conversacion = conversacionRepository
            .findBySessionId(sessionId)
            .orElseGet(() -> {
                Conversacion nueva = new Conversacion();
                nueva.setSessionId(sessionId);
                nueva.setCategoria(request.getCategoria());
                return conversacionRepository.save(nueva);
            });

        Mensaje msgUsuario = new Mensaje();
        msgUsuario.setConversacion(conversacion);
        msgUsuario.setRol("user");
        msgUsuario.setContenido(request.getMensaje());
        mensajeRepository.save(msgUsuario);

        String mensajeNormalizado = cacheService.normalizar(request.getMensaje());
        List<FAQ> faqs = faqRepository.findAll();
        FAQ faqEncontrada = null;
        for (FAQ faq : faqs) {
            String faqNormalizada = cacheService.normalizar(faq.getPregunta());
            if (faqNormalizada.equals(mensajeNormalizado)
                || cacheService.levenshtein(mensajeNormalizado, faqNormalizada) <= 3) {
                faqEncontrada = faq;
                break;
            }
        }

        String respuesta;
        String fuente;

        if (faqEncontrada != null) {
            respuesta = faqEncontrada.getRespuesta();
            fuente = "faq";
        } else {
            try {
                respuesta = ollamaService.generate(request.getMensaje(), SYSTEM_PROMPT);
                fuente = "ollama";
            } catch (Exception e) {
                respuesta = "El asistente esta reiniciandose. Vuelve a intentar en unos segundos.";
                fuente = "error";
            }
        }

        Mensaje msgAsistente = new Mensaje();
        msgAsistente.setConversacion(conversacion);
        msgAsistente.setRol("assistant");
        msgAsistente.setContenido(respuesta);
        mensajeRepository.save(msgAsistente);

        long tiempoMs = System.currentTimeMillis() - inicio;

        return new ChatResponse(respuesta, sessionId, tiempoMs, fuente);
    }
}
'@
Escribir-Java "service\ChatService.java" $contenido

# ============================================================
# 13/13 — DataInitializer.java (10 casos VES + 5 tramites)
# ============================================================
$contenido = @'
package com.redvecinalia.runner;

import com.redvecinalia.model.FAQ;
import com.redvecinalia.model.Tramite;
import com.redvecinalia.repository.FAQRepository;
import com.redvecinalia.repository.TramiteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FAQRepository faqRepository;
    private final TramiteRepository tramiteRepository;

    public DataInitializer(FAQRepository faqRepository, TramiteRepository tramiteRepository) {
        this.faqRepository = faqRepository;
        this.tramiteRepository = tramiteRepository;
    }

    @Override
    public void run(String... args) {
        if (faqRepository.count() > 0) {
            System.out.println("[DataInitializer] FAQ ya poblada (" + faqRepository.count() + " registros). Saltando carga.");
            return;
        }

        System.out.println("[DataInitializer] Poblando 10 FAQs de Villa El Salvador...");

        faqRepository.save(crearFaq("Tramite",
            "Como saco mi certificado de obra en la municipalidad de VES",
            "Acude a la oficina de Tramite Documentario de la Municipalidad de Villa El Salvador (Av. Juan Pablo II cdra. 1) con tu DNI, solicitud simple y recibo de luz. Horario: lun-vie 8:00-13:00 y 14:00-16:30."));

        faqRepository.save(crearFaq("Tramite",
            "Donde pago el impuesto predial",
            "En la caja de la Municipalidad de VES o en agencias del Banco de la Nacion con tu codigo de contribuyente. Tambien online en munives.gob.pe, seccion Tramites en linea, Impuesto predial."));

        faqRepository.save(crearFaq("Salud",
            "Donde esta el centro de salud mas cercano a La Union",
            "El Centro de Salud La Union (VES) esta en la Av. Central cdra. 10 de la AAHH La Union. Atiende lun-sab 7:00-13:00 y 14:00-19:00. Telefono: consultar directorio Minsa."));

        faqRepository.save(crearFaq("Salud",
            "Mi hijo tiene fiebre alta que centro atiende de noche",
            "Acude al servicio de emergencia del hospital mas cercano o llama a SAMU al 106 (linea gratuita, 24 horas). En VES, verifica que puesto de salud tiene turno nocturno."));

        faqRepository.save(crearFaq("Empleo",
            "Como formalizo mi negocio de comida en el parque",
            "Primero obten tu RUC en SUNAT (sunat.gob.pe o agencia VES). Luego solicita la licencia de funcionamiento en la municipalidad con tu RUC, DNI y recibo de luz. La inspeccion tecnica de seguridad es obligatoria para giros de alimentos."));

        faqRepository.save(crearFaq("Empleo",
            "Donde hay cursos tecnicos gratuitos en VES",
            "El CETPRO mas cercano ofrece carreras tecnicas gratuitas. Tambien el programa Jovenes a la Obra del MTPE convoca anualmente en VES. Revisar plataformas.empleo.gob.pe."));

        faqRepository.save(crearFaq("Empleo",
            "Como obtengo RUC",
            "Ingresa a sunat.gob.pe, seccion Tramites, Inscripcion al RUC. Necesitas DNI vigente y correo electronico. El tramite es inmediato y gratuito. Si prefieres atencion presencial, hay una agencia SUNAT en Lima Sur."));

        faqRepository.save(crearFaq("Denuncia",
            "Quiero reportar alumbrado publico malogrado",
            "Llama al 165 (linea gratuita de la municipalidad de VES) o reporta en munives.gob.pe, seccion Servicios, Reporte de alumbrado. Indica la direccion exacta del poste sin luz."));

        faqRepository.save(crearFaq("Denuncia",
            "Vi robos cerca del mercado a quien llamo",
            "Si hay delito en curso, marca 105 (PNP). Para denuncias formales, acude a la comisaria de VES. Tambien puedes contactar al serenazgo de VES para patrullaje inmediato en la zona."));

        faqRepository.save(crearFaq("Servicios",
            "No tengo agua hace 3 dias en la AAHH Union como reporto a Sedapal",
            "Llama al 119 (atencion Sedapal 24 horas) o reporta en sedapal.com.pe, seccion Reporte de averias. Indica tu direccion exacta y el tiempo sin servicio."));

        System.out.println("[DataInitializer] 10 FAQs cargadas correctamente.");

        if (tramiteRepository.count() == 0) {
            System.out.println("[DataInitializer] Poblando tramites...");

            tramiteRepository.save(crearTramite("Tramite",
                "Certificado de obra",
                "Documento que acredita la finalizacion de una obra constructiva.",
                "1. Reune DNI, solicitud simple y recibo de luz.\n2. Acude a Tramite Documentario de la Municipalidad de VES (Av. Juan Pablo II cdra. 1).\n3. Paga la tasa correspondiente.\n4. Recoge el certificado en el plazo indicado.",
                "https://munives.gob.pe"));

            tramiteRepository.save(crearTramite("Tramite",
                "Impuesto predial",
                "Tributo municipal anual sobre la propiedad de predios urbanos y rusticos.",
                "1. Obten tu codigo de contribuyente en la municipalidad.\n2. Paga en la caja de la Municipalidad de VES o en el Banco de la Nacion.\n3. Tambien puedes pagar online en munives.gob.pe.",
                "https://munives.gob.pe"));

            tramiteRepository.save(crearTramite("Empleo",
                "Formalizacion de negocio de alimentos",
                "Tramite para obtener licencia de funcionamiento de un negocio de comida.",
                "1. Obten tu RUC en SUNAT (sunat.gob.pe).\n2. Solicita licencia de funcionamiento en la Municipalidad de VES con RUC, DNI y recibo de luz.\n3. Aprueba la inspeccion tecnica de seguridad (obligatoria para alimentos).",
                "https://sunat.gob.pe"));

            tramiteRepository.save(crearTramite("Empleo",
                "Inscripcion al RUC",
                "Registro Unico de Contribuyentes, necesario para cualquier actividad economica formal.",
                "1. Ingresa a sunat.gob.pe, seccion Tramites, Inscripcion al RUC.\n2. Ten listo tu DNI vigente y correo electronico.\n3. El tramite es inmediato y gratuito.\n4. Alternativamente, acude a la agencia SUNAT en Lima Sur.",
                "https://sunat.gob.pe"));

            tramiteRepository.save(crearTramite("Empleo",
                "Cursos tecnicos gratuitos (CETPRO / Jovenes a la Obra)",
                "Programas de formacion tecnica gratuita para VES.",
                "1. Acude al CETPRO mas cercano para carreras tecnicas.\n2. Revisa la convocatoria anual de Jovenes a la Obra del MTPE.\n3. Postula en plataformas.empleo.gob.pe.",
                "https://plataformas.empleo.gob.pe"));

            System.out.println("[DataInitializer] 5 tramites cargados correctamente.");
        }

        System.out.println("[DataInitializer] Carga inicial completa.");
    }

    private FAQ crearFaq(String categoria, String pregunta, String respuesta) {
        FAQ faq = new FAQ();
        faq.setCategoria(categoria);
        faq.setPregunta(pregunta);
        faq.setRespuesta(respuesta);
        return faq;
    }

    private Tramite crearTramite(String categoria, String titulo, String descripcion, String pasos, String url) {
        Tramite t = new Tramite();
        t.setCategoria(categoria);
        t.setTitulo(titulo);
        t.setDescripcion(descripcion);
        t.setPasos(pasos);
        t.setUrl(url);
        return t;
    }
}
'@
Escribir-Java "runner\DataInitializer.java" $contenido

# ============================================================
# Resumen final
# ============================================================
Write-Host "`n=== Resumen ===" -ForegroundColor Cyan
Write-Host "Archivos creados: 13" -ForegroundColor Green
Write-Host "  repository\ : 4 archivos (Tramite, FAQ, Conversacion, Mensaje)" -ForegroundColor White
Write-Host "  dto\        : 2 archivos (ChatRequest, ChatResponse)" -ForegroundColor White
Write-Host "  controller\ : 3 archivos (Tramite, FAQ, Chat)" -ForegroundColor White
Write-Host "  service\    : 3 archivos (Ollama, Cache, Chat)" -ForegroundColor White
Write-Host "  runner\     : 1 archivo (DataInitializer con 10 FAQs + 5 tramites)" -ForegroundColor White

Write-Host "`n=== Proximo paso: compilar ===" -ForegroundColor Cyan
Write-Host "Ejecuta:" -ForegroundColor Yellow
Write-Host "  .\mvnw.cmd clean spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "Si compila sin errores, prueba con curl:" -ForegroundColor Yellow
Write-Host '  curl http://localhost:8080/api/health' -ForegroundColor White
Write-Host '  curl http://localhost:8080/api/faqs' -ForegroundColor White
Write-Host '  curl http://localhost:8080/api/tramites' -ForegroundColor White
Write-Host '  curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d "{\"mensaje\":\"donde pago el impuesto predial\",\"categoria\":\"Tramite\"}"' -ForegroundColor White
Write-Host ""
Write-Host "Listo. Backend completo." -ForegroundColor Green

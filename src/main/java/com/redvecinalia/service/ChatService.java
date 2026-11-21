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

	REGLAS CRITICAS (NUNCA VIOLAR):

    	1. NUNCA inventes datos. No inventes direcciones, telefonos, horarios,
       	nombres de instituciones ni URLs. Si no conoces un dato con certeza,
       	NO lo mencionas.

    	2. Solo usas informacion verificable y verificada:
       	- Telefonos oficiales: 105 (PNP), 106 (SAMU), 100 (MIMP violencia familiar),
         119 (Sedapal), 165 (municipalidad VES)
       	- URLs oficiales: solo .gob.pe, .edu.pe, .pe gubernamentales
       	- Direcciones confirmadas de instituciones REALES de Villa El Salvador

    	3. Si NO conoces la respuesta exacta, respondes obligatoriamente:
       	"No cuento con esa informacion registrada, pero te comparto esta alternativa
       	que puede servirte:" y luego das una opcion real y verificada relacionada.

    	4. Si te preguntan por algo fuera de tu alcance (tramites, denuncia, salud,
       	empleo, servicios publicos de VES), respondes amablemente que solo puedes
       	ayudar en esos cinco temas.

    	5. NUNCA uses direcciones de otros distritos (Lince, Jesus Maria, San Luis,
       	Miraflores, Surco, etc.). Solo Villa El Salvador y Lima Sur.

    	TUS RESPUESTAS:
    	- Breves (maximo 200 palabras)
    	- Concretas y accionables: si preguntan por un tramite, dices donde, cuando
      	y que documentos llevar
    	- Si preguntan por denuncia, das el telefono oficial o comisaria real
    	- Siempre terminas con un telefono o URL verificable cuando aplique

    	FUENTES VERIFICADAS QUE PUEDES CITAR:
    	- Municipalidad de VES: munives.gob.pe, Av. Juan Pablo II cdra. 1
    	- PNP: 105 (emergencias) o comisaria VES
    	- SAMU/Minsa: 106 (emergencias medicas)
    	- MIMP Linea 100: violencia familiar (gratis, 24h, confidencial)
    	- Sedapal: 119 (agua y saneamiento, 24h)
    	- SUNAT: sunat.gob.pe (RUC, tramites tributarios)
    	- Municipalidad VES linea 165: alumbrado publico, servicios municipales

	Si tienes dudas sobre la veracidad de un dato, es MEJOR decir
    	"No tengo esa informacion verificada, pero te sugiero contactar a [institucion real]"
    	que inventar algo.
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

        final String sessionId = (request.getSessionId() == null || request.getSessionId().isBlank())
            ? UUID.randomUUID().toString()
            : request.getSessionId();

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
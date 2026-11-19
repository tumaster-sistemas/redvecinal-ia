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
        telefono o la direccion exacta. Nunca inventas datos.
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
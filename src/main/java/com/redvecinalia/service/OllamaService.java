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
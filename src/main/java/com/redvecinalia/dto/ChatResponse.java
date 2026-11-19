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
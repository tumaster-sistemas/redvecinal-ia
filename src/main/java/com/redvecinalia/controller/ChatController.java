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
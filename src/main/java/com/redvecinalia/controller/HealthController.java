package com.redvecinalia.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("service", "Red Vecinal IA");
        response.put("version", "0.1.0");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("ods", new String[]{"ODS 10 - Inclusión digital", "ODS 5 - Prevención de violencia"});
        return response;
    }
}

package com.digiview.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check & Keep-Alive controller for Render deployment and cloud uptime monitoring.
 */
@RestController
public class HealthController {

    private final long startTime = System.currentTimeMillis();

    @GetMapping({"/", "/health", "/api/health"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "DigiView LMS Backend");
        response.put("version", "1.0.0");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/api/keep-alive", "/ping"})
    public ResponseEntity<Map<String, Object>> keepAlive() {
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ALIVE");
        response.put("service", "DigiView LMS Backend");
        response.put("uptimeSeconds", uptimeSeconds);
        response.put("uptimeFormatted", formatUptime(uptimeSeconds));
        response.put("timestamp", Instant.now().toString());
        response.put("renderExternalUrl", System.getenv("RENDER_EXTERNAL_URL"));
        response.put("message", "Render service is awake and operational.");
        return ResponseEntity.ok(response);
    }

    private String formatUptime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}

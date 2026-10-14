package com.digiview.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check, Web Dashboard, and Keep-Alive controller.
 * When accessed via a web browser (Accept: text/html), renders the rich Cyber Health Dashboard with Dino arcade game.
 * When accessed via REST or API (Accept: application/json), returns structured telemetry JSON for uptime monitors.
 */
@RestController
public class HealthController {

    private final long startTime = System.currentTimeMillis();
    private volatile String cachedHtml = null;

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Default server landing page, root redirection, and health check.
     * Accessible via '/', '/health', '/@', '/~', '/root'.
     * Content-negotiated: Serves the interactive HTML5 dashboard + Dino game to browsers,
     * or returns JSON telemetry to API monitors.
     */
    @GetMapping({"/", "/health", "/@", "/~", "/root"})
    public Object healthCheck(HttpServletRequest request,
                              @RequestHeader(value = "Accept", required = false) String acceptHeader) {
        // If request is from a web browser or default curl without JSON preference, deliver the HTML dashboard
        if (acceptHeader == null || acceptHeader.contains("text/html") || acceptHeader.equals("*/*")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(getHtmlContent());
        }
        // If explicitly requesting JSON, return the telemetry payload
        return ResponseEntity.ok(buildHealthReport());
    }

    /**
     * Root symbol redirection for sub-paths (e.g. /@/health -> /health, /@/api/health -> /api/health)
     */
    @GetMapping({"/@{subpath:[^.]*}", "/~{subpath:[^.]*}"})
    public ResponseEntity<Void> redirectSymbolSubpath(@PathVariable(required = false) String subpath) {
        String target = (subpath == null || subpath.trim().isEmpty() || subpath.equals("/")) ? "/" : subpath;
        if (!target.startsWith("/")) target = "/" + target;
        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(java.net.URI.create(target))
                .build();
    }

    /**
     * Dedicated REST API health endpoint (always returns JSON).
     */
    @GetMapping(value = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> healthJson() {
        return ResponseEntity.ok(buildHealthReport());
    }

    /**
     * Background Keep-Alive endpoint for Render.
     * Called by KeepAliveService or external monitors every 12 minutes to prevent idle spin-down.
     */
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

        // Quick memory snapshot
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMb = rt.maxMemory() / (1024 * 1024);
        response.put("memoryUsedMb", usedMb);
        response.put("memoryMaxMb", maxMb);

        return ResponseEntity.ok(response);
    }

    /**
     * Builds comprehensive health & telemetry report.
     */
    private Map<String, Object> buildHealthReport() {
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;

        Map<String, Object> report = new HashMap<>();
        report.put("status", "UP");
        report.put("service", "DigiView LMS Backend");
        report.put("version", "1.0.0");
        report.put("timestamp", Instant.now().toString());
        report.put("uptimeSeconds", uptimeSeconds);
        report.put("uptimeFormatted", formatUptime(uptimeSeconds));

        // JVM & Environment
        Runtime rt = Runtime.getRuntime();
        long totalMb = rt.totalMemory() / (1024 * 1024);
        long freeMb = rt.freeMemory() / (1024 * 1024);
        long usedMb = totalMb - freeMb;
        long maxMb = rt.maxMemory() / (1024 * 1024);

        Map<String, Object> jvm = new HashMap<>();
        jvm.put("usedMemoryMb", usedMb);
        jvm.put("totalMemoryMb", totalMb);
        jvm.put("freeMemoryMb", freeMb);
        jvm.put("maxMemoryMb", maxMb);
        jvm.put("threadCount", Thread.activeCount());
        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("osName", System.getProperty("os.name"));
        jvm.put("springBootVersion", SpringBootVersion.getVersion());
        report.put("jvm", jvm);

        // Database
        Map<String, Object> db = new HashMap<>();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                boolean valid = conn.isValid(2);
                db.put("status", valid ? "CONNECTED" : "DEGRADED");
                db.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
                db.put("pool", "HikariCP SSL Active");
            } catch (Exception e) {
                db.put("status", "DISCONNECTED");
                db.put("error", e.getMessage());
            }
        } else {
            db.put("status", "STANDALONE");
            db.put("note", "No DataSource active");
        }
        report.put("database", db);

        // Cloud / Render
        Map<String, Object> cloud = new HashMap<>();
        String renderUrl = System.getenv("RENDER_EXTERNAL_URL");
        cloud.put("provider", renderUrl != null ? "Render Cloud" : "Local Development");
        cloud.put("externalUrl", renderUrl != null ? renderUrl : "http://localhost:8085");
        cloud.put("keepAliveScheduler", "Active (12m interval)");
        report.put("cloud", cloud);

        return report;
    }

    /**
     * Reads static/index.html with in-memory caching.
     */
    private String getHtmlContent() {
        if (cachedHtml != null) {
            return cachedHtml;
        }
        try {
            ClassPathResource resource = new ClassPathResource("static/index.html");
            try (InputStream is = resource.getInputStream()) {
                cachedHtml = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
                return cachedHtml;
            }
        } catch (Exception e) {
            return "<!DOCTYPE html><html><body style='background:#050B14;color:#38BDF8;font-family:sans-serif;padding:40px;'>"
                 + "<h1>DigiView LMS Server: UP</h1><p>Status: Healthy (200 OK)</p>"
                 + "<p><a href='/api/health' style='color:#fff;'>View JSON Telemetry</a></p></body></html>";
        }
    }

    private String formatUptime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}

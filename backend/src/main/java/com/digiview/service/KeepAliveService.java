package com.digiview.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Background Keep-Alive Service for Render Web Service.
 * Render Free tier spins down services after 15 minutes of inactivity.
 * This service automatically pings its public Render URL every 12 minutes
 * through Render's external proxy, resetting the 15-minute idle countdown timer.
 */
@Service
public class KeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveService.class);

    // Initial delay of 2 minutes, then ping every 12 minutes (720,000 ms)
    @Scheduled(initialDelay = 120_000, fixedRate = 720_000)
    public void pingSelf() {
        String publicUrl = getTargetUrl();
        if (publicUrl == null || publicUrl.trim().isEmpty()) {
            log.debug("[KeepAlive] Running locally or RENDER_EXTERNAL_URL not set; skipping self-ping.");
            return;
        }

        String pingEndpoint = publicUrl.endsWith("/") ? publicUrl + "api/keep-alive" : publicUrl + "/api/keep-alive";
        try {
            URL url = new URL(pingEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "DigiView-KeepAlive-Ping/1.0");

            int code = conn.getResponseCode();
            if (code >= 200 && code < 400) {
                log.info("[KeepAlive] Successfully pinged {} (HTTP {}) - Render service kept alive.", pingEndpoint, code);
            } else {
                log.warn("[KeepAlive] Ping to {} returned status: HTTP {}", pingEndpoint, code);
            }
        } catch (Exception e) {
            log.warn("[KeepAlive] Could not ping {}: {}", pingEndpoint, e.getMessage());
        }
    }

    private String getTargetUrl() {
        // 1. Check Render's auto-injected external URL (e.g. https://digiview-backend.onrender.com)
        String renderUrl = System.getenv("RENDER_EXTERNAL_URL");
        if (renderUrl != null && !renderUrl.trim().isEmpty()) {
            return renderUrl.trim();
        }
        // 2. Check custom user-defined KEEP_ALIVE_URL
        String customUrl = System.getenv("KEEP_ALIVE_URL");
        if (customUrl != null && !customUrl.trim().isEmpty()) {
            return customUrl.trim();
        }
        return null;
    }
}

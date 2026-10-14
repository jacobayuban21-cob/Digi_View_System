package System;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Lightweight HTTP client for DigiView Desktop.
 * Connects to the DigiView Spring Boot Backend (cloud deployed on Render or local dev).
 * Persists custom server URLs to digiview-config.properties so cloud connections are remembered.
 * Uses standard java.net.HttpURLConnection (Java 8+ compatible, 0 external dependencies).
 */
public class ApiClient {

    public static final String DEFAULT_CLOUD_URL = "https://digiview-backend.onrender.com";
    public static final String DEFAULT_LOCAL_URL = "http://localhost:8085";
    private static final String CONFIG_FILE_NAME = "digiview-config.properties";

    private static String baseUrl = initBaseUrl();

    private static String initBaseUrl() {
        // 1. Check system property
        String prop = System.getProperty("digiview.api.url");
        if (prop != null && !prop.trim().isEmpty()) return cleanUrl(prop);

        // 2. Check environment variable
        String env = System.getenv("DIGIVIEW_API_URL");
        if (env != null && !env.trim().isEmpty()) return cleanUrl(env);

        // 3. Check persistent config file in working dir or user home
        String fromFile = loadConfigUrl();
        if (fromFile != null && !fromFile.trim().isEmpty()) return cleanUrl(fromFile);

        // 4. Default to Render cloud URL (or local if explicitly developing locally)
        return DEFAULT_CLOUD_URL;
    }

    public static String cleanUrl(String url) {
        if (url == null) return DEFAULT_CLOUD_URL;
        url = url.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static void setBaseUrl(String url) {
        baseUrl = cleanUrl(url);
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void saveConfigUrl(String url) {
        String cleaned = cleanUrl(url);
        baseUrl = cleaned;
        try {
            Properties p = new Properties();
            File cfgFile = getConfigFile();
            if (cfgFile.exists()) {
                try (FileInputStream fis = new FileInputStream(cfgFile)) {
                    p.load(fis);
                }
            }
            p.setProperty("server.url", cleaned);
            try (FileOutputStream fos = new FileOutputStream(cfgFile)) {
                p.store(fos, "DigiView Server Configuration");
            }
        } catch (Exception ignored) {}
    }

    private static String loadConfigUrl() {
        try {
            File cfgFile = getConfigFile();
            if (cfgFile.exists()) {
                Properties p = new Properties();
                try (FileInputStream fis = new FileInputStream(cfgFile)) {
                    p.load(fis);
                    String u = p.getProperty("server.url");
                    if (u != null && !u.trim().isEmpty()) return u.trim();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static File getConfigFile() {
        File local = new File(CONFIG_FILE_NAME);
        if (local.canWrite() || (!local.exists() && new File(".").canWrite())) {
            return local;
        }
        return new File(System.getProperty("user.home"), CONFIG_FILE_NAME);
    }

    public static class ConnectionTestResult {
        public final boolean success;
        public final int httpCode;
        public final long latencyMs;
        public final String message;

        public ConnectionTestResult(boolean success, int httpCode, long latencyMs, String message) {
            this.success = success;
            this.httpCode = httpCode;
            this.latencyMs = latencyMs;
            this.message = message;
        }
    }

    public static ConnectionTestResult testConnection(String targetUrl) {
        String urlStr = cleanUrl(targetUrl);
        long start = System.currentTimeMillis();
        try {
            URL url = new URL(urlStr + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "DigiView-Desktop/1.0");

            int code = conn.getResponseCode();
            long latency = System.currentTimeMillis() - start;
            if (code >= 200 && code < 400) {
                return new ConnectionTestResult(true, code, latency, "Server is UP and responsive.");
            } else {
                return new ConnectionTestResult(false, code, latency, "Server returned HTTP " + code);
            }
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            return new ConnectionTestResult(false, -1, latency, "Could not connect: " + e.getMessage());
        }
    }

    public static boolean isServerAvailable() {
        ConnectionTestResult res = testConnection(baseUrl);
        if (res.success) return true;

        // Auto fallback test
        if (baseUrl.equals(DEFAULT_CLOUD_URL)) {
            ConnectionTestResult localRes = testConnection(DEFAULT_LOCAL_URL);
            if (localRes.success) {
                baseUrl = DEFAULT_LOCAL_URL;
                return true;
            }
        } else if (baseUrl.contains("localhost")) {
            ConnectionTestResult cloudRes = testConnection(DEFAULT_CLOUD_URL);
            if (cloudRes.success) {
                baseUrl = DEFAULT_CLOUD_URL;
                return true;
            }
        }
        return false;
    }

    public static boolean registerUser(String username, String password, String fullName, int age, String gender, String strand, String gradeSection) {
        try {
            URL url = new URL(baseUrl + "/api/auth/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            String json = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"fullName\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"strand\":\"%s\",\"gradeSection\":\"%s\"}",
                escapeJson(username), escapeJson(password), escapeJson(fullName), age, escapeJson(gender), escapeJson(strand), escapeJson(gradeSection)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            return conn.getResponseCode() >= 200 && conn.getResponseCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean submitQuizRecord(String username, String quiz, int score, int total, int percent, String date) {
        try {
            URL url = new URL(baseUrl + "/api/quiz/record");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            String json = String.format(
                "{\"username\":\"%s\",\"quiz\":\"%s\",\"score\":%d,\"total\":%d,\"percent\":%d,\"date\":\"%s\"}",
                escapeJson(username), escapeJson(quiz), score, total, percent, escapeJson(date)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            return conn.getResponseCode() >= 200 && conn.getResponseCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean syncXp(String username, int xpAmount) {
        try {
            URL url = new URL(baseUrl + "/api/users/" + URLEncoder.encode(username, "UTF-8") + "/xp?amount=" + xpAmount);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean unlockAchievement(String username, String achievement) {
        try {
            URL url = new URL(baseUrl + "/api/users/" + URLEncoder.encode(username, "UTF-8") + "/achievement?achievement=" + URLEncoder.encode(achievement, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}

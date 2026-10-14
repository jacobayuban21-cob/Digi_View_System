package System;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight HTTP client for DigiView Desktop
 * Connects to the DigiView Spring Boot Backend (auto-detects port 8085, 8080, or custom DIGIVIEW_API_URL)
 * Uses standard java.net.HttpURLConnection (Java 8+ compatible, 0 external dependencies).
 */
public class ApiClient {

    private static String baseUrl = initBaseUrl();

    private static String initBaseUrl() {
        String prop = System.getProperty("digiview.api.url");
        if (prop != null && !prop.trim().isEmpty()) return prop.trim();
        String env = System.getenv("DIGIVIEW_API_URL");
        if (env != null && !env.trim().isEmpty()) return env.trim();
        return "http://localhost:8085";
    }

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static boolean isServerAvailable() {
        try {
            URL url = new URL(baseUrl + "/api/leaderboard");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1500);
            conn.setReadTimeout(1500);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            return code >= 200 && code < 400;
        } catch (Exception e) {
            if (baseUrl.contains(":8085")) {
                try {
                    URL fallbackUrl = new URL("http://localhost:8080/api/leaderboard");
                    HttpURLConnection fallbackConn = (HttpURLConnection) fallbackUrl.openConnection();
                    fallbackConn.setConnectTimeout(1000);
                    fallbackConn.setRequestMethod("GET");
                    if (fallbackConn.getResponseCode() >= 200 && fallbackConn.getResponseCode() < 400) {
                        baseUrl = "http://localhost:8080";
                        return true;
                    }
                } catch (Exception ignored) {}
            }
            return false;
        }
    }

    public static boolean registerUser(String username, String password, String fullName, int age, String gender, String strand, String gradeSection) {
        try {
            URL url = new URL(baseUrl + "/api/auth/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

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
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            String json = String.format(
                "{\"username\":\"%s\",\"quiz\":\"%s\",\"score\":%d,\"total\":%d,\"percent\":%d,\"date\":\"%s\"}",
                escapeJson(username), escapeJson(quiz), score, total, percent, escapeJson(date)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean syncXp(String username, int xpAmount) {
        try {
            URL url = new URL(baseUrl + "/api/users/" + URLEncoder.encode(username, "UTF-8") + "/xp?amount=" + xpAmount);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(2000);
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
            conn.setConnectTimeout(2000);
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


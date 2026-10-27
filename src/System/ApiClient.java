package System;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.net.ssl.*;

/**
 * Lightweight HTTP client for DigiView Desktop.
 * Connects to the DigiView Spring Boot Backend (cloud deployed on Render or local dev).
 * Persists custom server URLs to digiview-config.properties so cloud connections are remembered.
 * Uses standard java.net.HttpURLConnection (Java 8+ compatible, 0 external dependencies).
 * Enforces modern TLS (TLSv1.2, TLSv1.3), SNI extension, and smart local/cloud fallback.
 */
public class ApiClient {

    public static final String DEFAULT_CLOUD_URL = "https://digi-view-system.onrender.com";
    public static final String DEFAULT_LOCAL_URL = "http://localhost:8085";
    private static final String CONFIG_FILE_NAME = "digiview-config.properties";

    private static String baseUrl = initBaseUrl();
    private static volatile boolean sslConfigured = false;

    static {
        setupSslEnvironment();
    }

    /**
     * Configures the JVM to enforce modern TLS 1.2/1.3, enable SNI extensions,
     * and allow TLS renegotiation required by Cloudflare/Render edge routers.
     */
    public static synchronized void setupSslEnvironment() {
        if (sslConfigured) return;
        try {
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
            System.setProperty("jsse.enableSNIExtension", "true");
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.setProperty("jdk.tls.rejectClientInitiatedRenegotiation", "false");

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, new SecureRandom());
            CustomTlsSocketFactory sf = new CustomTlsSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(sf);
            sslConfigured = true;
        } catch (Exception e) {
            log("SSL", "TLS initialization notice: " + e.getMessage());
        }
    }

    private static class CustomTlsSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;

        public CustomTlsSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        private Socket patch(Socket s) {
            if (s instanceof SSLSocket) {
                SSLSocket ssl = (SSLSocket) s;
                String[] supported = ssl.getSupportedProtocols();
                List<String> enabled = new ArrayList<String>();
                for (String p : supported) {
                    if ("TLSv1.3".equals(p) || "TLSv1.2".equals(p) || "TLSv1.1".equals(p) || "TLSv1".equals(p)) {
                        enabled.add(p);
                    }
                }
                if (!enabled.isEmpty()) {
                    ssl.setEnabledProtocols(enabled.toArray(new String[0]));
                }
                try {
                    String[] ciphers = ssl.getSupportedCipherSuites();
                    if (ciphers != null && ciphers.length > 0) {
                        ssl.setEnabledCipherSuites(ciphers);
                    }
                } catch (Exception ignored) {}
            }
            return s;
        }

        @Override public String[] getDefaultCipherSuites() { return delegate.getDefaultCipherSuites(); }
        @Override public String[] getSupportedCipherSuites() { return delegate.getSupportedCipherSuites(); }
        @Override public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException { return patch(delegate.createSocket(s, host, port, autoClose)); }
        @Override public Socket createSocket(String host, int port) throws IOException { return patch(delegate.createSocket(host, port)); }
        @Override public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException { return patch(delegate.createSocket(host, port, localHost, localPort)); }
        @Override public Socket createSocket(InetAddress host, int port) throws IOException { return patch(delegate.createSocket(host, port)); }
        @Override public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException { return patch(delegate.createSocket(address, port, localAddress, localPort)); }
    }

    private static String initBaseUrl() {
        // 1. Check system property
        String prop = System.getProperty("digiview.api.url");
        if (prop != null && !prop.trim().isEmpty()) return cleanUrl(prop);

        // 2. Check environment variable
        String env = System.getenv("DIGIVIEW_API_URL");
        if (env != null && !env.trim().isEmpty()) return cleanUrl(env);

        // 3. Check persistent config file in working dir or user home
        String fromFile = loadConfigUrl();
        if (fromFile != null && !fromFile.trim().isEmpty()) {
            if (fromFile.contains("digiview-backend.onrender.com")) {
                saveConfigUrl(DEFAULT_CLOUD_URL);
                return DEFAULT_CLOUD_URL;
            }
            return cleanUrl(fromFile);
        }

        // 4. Default to Render cloud URL
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

    public static void log(String tag, String message) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
        System.out.printf("[%s] [%-8s] %s%n", timestamp, tag, message);
    }

    public static void setBaseUrl(String url) {
        baseUrl = cleanUrl(url);
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns a site-local address for this computer so another device on the
     * same network can connect to a backend started with digiview local.
     */
    public static String getLocalLanUrl() {
        try {
            java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) continue;
                java.util.Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
                        return "http://" + address.getHostAddress() + ":8085";
                    }
                }
            }
        } catch (SocketException e) {
            log("API", "Unable to detect LAN address: " + e.getMessage());
        }
        return DEFAULT_LOCAL_URL;
    }

    /**
     * Unified HTTP connection factory with modern headers and SSL/TLS configuration.
     */
    public static HttpURLConnection openConnection(String targetUrl, String method) throws IOException {
        setupSslEnvironment();
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(15000); // 15 seconds allows Render cold start
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) DigiView/1.1 (Desktop)");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        return conn;
    }

    /**
     * Resolves an endpoint path with root symbol redirection.
     * Examples:
     * - "@/api/auth/login" -> "https://digi-view-system.onrender.com/api/auth/login"
     * - "@/health"         -> "https://digi-view-system.onrender.com/health"
     * - "~/api/quiz"       -> "https://digi-view-system.onrender.com/api/quiz"
     */
    public static String redirectRootUrl(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return getBaseUrl();
        }
        String p = endpoint.trim();
        while (p.startsWith("@") || p.startsWith("~") || p.startsWith("^") || p.startsWith("$")) {
            p = p.substring(1).trim();
        }
        while (p.startsWith("/") || p.startsWith("\\")) {
            p = p.substring(1).trim();
        }
        return getBaseUrl() + "/" + p;
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
            p.setProperty("digiview.api.url", cleaned);
            try (FileOutputStream fos = new FileOutputStream(cfgFile)) {
                p.store(fos, "DigiView Server Configuration");
            }
        } catch (Exception ignored) {}
    }

    private static String loadConfigUrl() {
        File[] candidates = new File[] {
            new File("config", CONFIG_FILE_NAME),
            new File(CONFIG_FILE_NAME),
            new File(System.getProperty("user.home"), CONFIG_FILE_NAME)
        };
        for (File cfgFile : candidates) {
            if (cfgFile != null && cfgFile.exists() && cfgFile.isFile()) {
                try {
                    Properties p = new Properties();
                    try (FileInputStream fis = new FileInputStream(cfgFile)) {
                        p.load(fis);
                        String u = p.getProperty("server.url");
                        if (u == null || u.trim().isEmpty()) {
                            u = p.getProperty("digiview.api.url");
                        }
                        if (u != null && !u.trim().isEmpty()) return u.trim();
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static File getConfigFile() {
        // 1. Primary location: config/digiview-config.properties
        File cfgDirFile = new File("config", CONFIG_FILE_NAME);
        if (cfgDirFile.exists() && cfgDirFile.canWrite()) {
            return cfgDirFile;
        }
        // 2. Legacy fallback: root digiview-config.properties
        File localRoot = new File(CONFIG_FILE_NAME);
        if (localRoot.exists() && localRoot.canWrite()) {
            return localRoot;
        }
        // 3. If config/ exists and is writable, create in config/
        File cfgDir = new File("config");
        if (cfgDir.isDirectory() && cfgDir.canWrite()) {
            return cfgDirFile;
        }
        // 4. Try creating config/ directory if root is writable
        if (new File(".").canWrite()) {
            try {
                if (!cfgDir.exists()) cfgDir.mkdirs();
                if (cfgDir.isDirectory() && cfgDir.canWrite()) {
                    return cfgDirFile;
                }
            } catch (Exception ignored) {}
            return localRoot;
        }
        // 5. Fallback to user home directory
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
            HttpURLConnection conn = openConnection(urlStr + "/health", "GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            int code = conn.getResponseCode();
            long latency = System.currentTimeMillis() - start;
            log("API", "GET " + urlStr + "/health -> HTTP " + code + " (" + latency + "ms)");
            if (code >= 200 && code < 400) {
                return new ConnectionTestResult(true, code, latency, "Server is UP and responsive.");
            } else {
                return new ConnectionTestResult(false, code, latency, "Server returned HTTP " + code);
            }
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log("API", "GET " + urlStr + "/health -> FAILED (" + latency + "ms): " + e.getMessage());
            return new ConnectionTestResult(false, -1, latency, formatFriendlyError(e.getMessage(), urlStr));
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

    private static String formatFriendlyError(String rawErr, String targetUrl) {
        String err = rawErr != null ? rawErr : "Unknown error";
        if (err.contains("handshake_failure") || err.contains("SSLHandshakeException") || err.contains("fatal alert")) {
            return "Secure SSL connection failed (TLS handshake alert).\n"
                 + "Please check your internet or run scripts\\allow-firewall.bat to verify network access.";
        } else if (err.contains("timed out") || err.contains("Timeout") || err.contains("SocketTimeoutException")) {
            return "Connection timed out reaching " + targetUrl + ".\n"
                 + "Cloud server may be spinning up from standby. Please retry in 20-30 seconds.";
        } else if (err.contains("Connection refused") || err.contains("ConnectException")) {
            return "Could not reach server at " + targetUrl + ".\n"
                 + "Server appears to be offline. Verify the server URL or start the local backend.";
        }
        return "Could not connect to DigiView server: " + err;
    }

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final String username;
        public final String fullName;
        public final String gradeSection;
        public final String gender;
        public final int xp;

        public AuthResult(boolean success, String message, String username, String fullName, String gradeSection, String gender, int xp) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.fullName = fullName;
            this.gradeSection = gradeSection;
            this.gender = gender;
            this.xp = xp;
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null, null, null, null, 0);
        }
    }

    public static AuthResult login(String username, String password) {
        return loginInternal(username, password, true);
    }

    private static AuthResult loginInternal(String username, String password, boolean allowLocalRetry) {
        try {
            HttpURLConnection conn = openConnection(redirectRootUrl("/api/auth/login"), "POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", escapeJson(username), escapeJson(password));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String responseStr = readStream(is);

            boolean success = code >= 200 && code < 300;
            String msg = parseJsonString(responseStr, "message");
            if (msg.isEmpty()) msg = success ? "Success" : "Invalid username or password.";

            log("API", "POST /api/auth/login for '" + username + "' -> HTTP " + code + " (" + (success ? "AUTHORIZED" : msg) + ")");

            if (success) {
                String u = parseJsonString(responseStr, "username");
                if (u.isEmpty()) u = username;
                String fn = parseJsonString(responseStr, "fullName");
                if (fn.isEmpty()) fn = u;
                String gs = parseJsonString(responseStr, "gradeSection");
                if (gs.isEmpty()) gs = "Grade 11";
                String gen = parseJsonString(responseStr, "gender");
                if (gen.isEmpty()) gen = "Not specified";
                int xp = parseJsonInt(responseStr, "xp", 0);
                return new AuthResult(true, msg, u, fn, gs, gen, xp);
            } else {
                return AuthResult.failure(msg);
            }
        } catch (Exception e) {
            log("API", "POST /api/auth/login failed: " + e.getMessage());

            // If connecting to cloud fails, auto-test if local backend is available
            if (allowLocalRetry && baseUrl.equals(DEFAULT_CLOUD_URL)) {
                try {
                    ConnectionTestResult localCheck = testConnection(DEFAULT_LOCAL_URL);
                    if (localCheck.success) {
                        log("API", "Cloud server unreachable, automatically retrying with active local backend: " + DEFAULT_LOCAL_URL);
                        baseUrl = DEFAULT_LOCAL_URL;
                        return loginInternal(username, password, false);
                    }
                } catch (Exception ignored) {}
            }

            return AuthResult.failure(formatFriendlyError(e.getMessage(), baseUrl));
        }
    }

    public static AuthResult register(String username, String password, String fullName, int age, String gender, String strand, String gradeSection) {
        return registerInternal(username, password, fullName, age, gender, strand, gradeSection, true);
    }

    private static AuthResult registerInternal(String username, String password, String fullName, int age, String gender, String strand, String gradeSection, boolean allowLocalRetry) {
        try {
            HttpURLConnection conn = openConnection(redirectRootUrl("/api/auth/register"), "POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"fullName\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"strand\":\"%s\",\"gradeSection\":\"%s\"}",
                escapeJson(username), escapeJson(password), escapeJson(fullName), age, escapeJson(gender), escapeJson(strand), escapeJson(gradeSection)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String responseStr = readStream(is);

            boolean success = code >= 200 && code < 300;
            String msg = parseJsonString(responseStr, "message");
            if (msg.isEmpty()) msg = success ? "Success" : "Registration failed.";

            log("API", "POST /api/auth/register for '" + username + "' -> HTTP " + code + " (" + (success ? "CREATED" : msg) + ")");

            if (success) {
                String u = parseJsonString(responseStr, "username");
                if (u.isEmpty()) u = username;
                String fn = parseJsonString(responseStr, "fullName");
                if (fn.isEmpty()) fn = fullName;
                String gs = parseJsonString(responseStr, "gradeSection");
                if (gs.isEmpty()) gs = gradeSection;
                String gen = parseJsonString(responseStr, "gender");
                if (gen.isEmpty()) gen = gender;
                int xp = parseJsonInt(responseStr, "xp", 0);
                return new AuthResult(true, msg, u, fn, gs, gen, xp);
            } else {
                return AuthResult.failure(msg);
            }
        } catch (Exception e) {
            log("API", "POST /api/auth/register failed: " + e.getMessage());

            if (allowLocalRetry && baseUrl.equals(DEFAULT_CLOUD_URL)) {
                try {
                    ConnectionTestResult localCheck = testConnection(DEFAULT_LOCAL_URL);
                    if (localCheck.success) {
                        log("API", "Cloud server unreachable, automatically retrying registration with local backend: " + DEFAULT_LOCAL_URL);
                        baseUrl = DEFAULT_LOCAL_URL;
                        return registerInternal(username, password, fullName, age, gender, strand, gradeSection, false);
                    }
                } catch (Exception ignored) {}
            }

            return AuthResult.failure(formatFriendlyError(e.getMessage(), baseUrl));
        }
    }

    public static boolean registerUser(String username, String password, String fullName, int age, String gender, String strand, String gradeSection) {
        AuthResult res = register(username, password, fullName, age, gender, strand, gradeSection);
        return res.success;
    }

    public static boolean submitQuizRecord(String username, String quiz, int score, int total, int percent, String date) {
        try {
            HttpURLConnection conn = openConnection(redirectRootUrl("/api/quiz/record"), "POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format(
                "{\"username\":\"%s\",\"quiz\":\"%s\",\"score\":%d,\"total\":%d,\"percent\":%d,\"date\":\"%s\"}",
                escapeJson(username), escapeJson(quiz), score, total, percent, escapeJson(date)
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            boolean ok = code >= 200 && code < 300;
            log("API", "POST /api/quiz/record for user '" + username + "' (" + quiz + ", " + score + "/" + total + ") -> HTTP " + code);
            return ok;
        } catch (Exception e) {
            log("API", "POST /api/quiz/record failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean syncXp(String username, int xpAmount) {
        try {
            HttpURLConnection conn = openConnection(
                redirectRootUrl("/api/users/" + URLEncoder.encode(username, "UTF-8") + "/xp?amount=" + xpAmount),
                "POST"
            );
            conn.setConnectTimeout(6000);
            int code = conn.getResponseCode();
            log("API", "POST /api/users/" + username + "/xp?amount=" + xpAmount + " -> HTTP " + code);
            return code == 200;
        } catch (Exception e) {
            log("API", "POST syncXp failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean unlockAchievement(String username, String achievement) {
        try {
            HttpURLConnection conn = openConnection(
                redirectRootUrl("/api/users/" + URLEncoder.encode(username, "UTF-8") + "/achievement?achievement=" + URLEncoder.encode(achievement, "UTF-8")),
                "POST"
            );
            conn.setConnectTimeout(6000);
            int code = conn.getResponseCode();
            log("API", "POST /api/users/" + username + "/achievement (" + achievement + ") -> HTTP " + code);
            return code == 200;
        } catch (Exception e) {
            log("API", "POST unlockAchievement failed: " + e.getMessage());
            return false;
        }
    }

    private static String readStream(InputStream is) {
        if (is == null) return "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String parseJsonString(String json, String key) {
        if (json == null || key == null) return "";
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static int parseJsonInt(String json, String key, int defVal) {
        if (json == null || key == null) return defVal;
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return defVal;
        start += pattern.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (start == end) return defVal;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return defVal;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}

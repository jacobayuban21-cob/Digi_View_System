package com.digiview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class DigiViewApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(DigiViewApplication.class, args);
    }

    public static void loadDotEnvIfPresent() {
        File[] candidates = new File[]{
            new File(".env"),
            new File("backend/.env"),
            new File("../backend/.env"),
            new File("../../backend/.env")
        };
        for (File f : candidates) {
            if (f.exists() && f.isFile()) {
                try {
                    List<String> lines = Files.readAllLines(f.toPath());
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                        int eq = trimmed.indexOf('=');
                        if (eq > 0) {
                            String key = trimmed.substring(0, eq).trim();
                            String val = trimmed.substring(eq + 1).trim();
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                    System.out.println("[DigiView] Successfully loaded credentials from: " + f.getAbsolutePath());
                    break;
                } catch (Exception ignored) {}
            }
        }
    }
}


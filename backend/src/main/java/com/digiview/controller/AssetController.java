package com.digiview.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal Asset Controller for DigiView Server.
 * Guarantees that all icons, images, sounds, and favicons are delivered
 * reliably with proper MIME types, caching headers, and case-insensitivity.
 */
@RestController
public class AssetController {

    private final ConcurrentHashMap<String, byte[]> assetCache = new ConcurrentHashMap<>();

    private static final java.util.Map<String, String> EXACT_NAMES = java.util.Map.of(
        "splash-icon.png", "Splash-icon.png",
        "icon.jpg", "Icon.jpg",
        "icon.ico", "Icon.ico",
        "favicon.ico", "Favicon.ico",
        "favicon.png", "Splash-icon.png"
    );

    @GetMapping({
        "/favicon.ico", "/Favicon.ico", "/favicon.png",
        "/Splash-icon.png", "/splash-icon.png",
        "/Icon.jpg", "/icon.jpg",
        "/Icon.ico", "/icon.ico",
        "/{filename:.*\\.(?:png|jpg|jpeg|ico|svg|gif|webp)}"
    })
    public ResponseEntity<byte[]> getRootAsset(jakarta.servlet.http.HttpServletRequest request) {
        String uri = request.getRequestURI();
        String filename = uri.substring(uri.lastIndexOf('/') + 1);
        return deliverAsset(filename, "Icons/" + filename, "icons/" + filename);
    }

    @GetMapping({
        "/Icons/{filename:.*}",
        "/icons/{filename:.*}",
        "/public/Icons/{filename:.*}",
        "/public/icons/{filename:.*}",
        "/@/Icons/{filename:.*}",
        "/@/icons/{filename:.*}",
        "/@/public/Icons/{filename:.*}",
        "/@/public/icons/{filename:.*}"
    })
    public ResponseEntity<byte[]> getIconAsset(@PathVariable String filename) {
        return deliverAsset("Icons/" + filename, "icons/" + filename, filename);
    }

    @GetMapping({
        "/sounds/{filename:.*}",
        "/Sounds/{filename:.*}",
        "/public/sounds/{filename:.*}",
        "/public/Sounds/{filename:.*}",
        "/@/sounds/{filename:.*}",
        "/@/Sounds/{filename:.*}",
        "/@/public/sounds/{filename:.*}",
        "/@/public/Sounds/{filename:.*}"
    })
    public ResponseEntity<byte[]> getSoundAsset(@PathVariable String filename) {
        return deliverAsset("sounds/" + filename, "Sounds/" + filename, filename);
    }

    private ResponseEntity<byte[]> deliverAsset(String... pathCandidates) {
        for (String rel : pathCandidates) {
            byte[] cached = assetCache.get(rel.toLowerCase());
            if (cached != null) {
                return buildResponse(cached, rel);
            }
        }

        for (String rel : pathCandidates) {
            byte[] data = loadAssetBytes(rel);
            if (data != null && data.length > 0) {
                assetCache.put(rel.toLowerCase(), data);
                return buildResponse(data, rel);
            }
        }

        // Additional case-tolerant fallbacks
        for (String rel : pathCandidates) {
            String basename = rel.contains("/") ? rel.substring(rel.lastIndexOf('/') + 1) : rel;
            String exactName = EXACT_NAMES.getOrDefault(basename.toLowerCase(), basename);
            String[] commonPaths = new String[]{
                "static/Icons/" + exactName,
                "static/" + exactName,
                "static/sounds/" + exactName,
                "static/icons/" + exactName,
                "static/Icons/" + basename,
                "static/" + basename,
                "static/sounds/" + basename,
                "static/icons/" + basename
            };
            for (String cp : commonPaths) {
                byte[] data = loadAssetBytes(cp);
                if (data != null && data.length > 0) {
                    assetCache.put(rel.toLowerCase(), data);
                    return buildResponse(data, exactName);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private byte[] loadAssetBytes(String path) {
        String basename = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
        String exactName = EXACT_NAMES.getOrDefault(basename.toLowerCase(), basename);

        // 1. Try ClassPathResource in static/ and exact variants
        String[] cprCandidates = new String[]{
            path.startsWith("static/") ? path : "static/" + path,
            path,
            "static/Icons/" + exactName,
            "static/" + exactName,
            "static/sounds/" + exactName,
            "static/icons/" + exactName
        };
        for (String cprPath : cprCandidates) {
            try {
                ClassPathResource cpr = new ClassPathResource(cprPath);
                if (cpr.exists()) {
                    try (InputStream is = cpr.getInputStream()) {
                        return StreamUtils.copyToByteArray(is);
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Try filesystem relative to backend and root
        String[] fileCandidates = new String[]{
            path,
            "public/" + path,
            "../public/" + path,
            "src/main/resources/static/" + path,
            "../backend/src/main/resources/static/" + path
        };
        for (String fp : fileCandidates) {
            try {
                File f = new File(fp);
                if (f.exists() && f.isFile()) {
                    try (InputStream is = new FileInputStream(f)) {
                        return StreamUtils.copyToByteArray(is);
                    }
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    private ResponseEntity<byte[]> buildResponse(byte[] data, String path) {
        String lower = path.toLowerCase();
        MediaType mediaType;
        if (lower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".ico")) {
            mediaType = MediaType.parseMediaType("image/x-icon");
        } else if (lower.endsWith(".svg")) {
            mediaType = MediaType.parseMediaType("image/svg+xml");
        } else if (lower.endsWith(".wav")) {
            mediaType = MediaType.parseMediaType("audio/wav");
        } else if (lower.endsWith(".mp3")) {
            mediaType = MediaType.parseMediaType("audio/mpeg");
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .header("Access-Control-Allow-Origin", "*")
                .body(data);
    }
}

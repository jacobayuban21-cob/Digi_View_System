package com.digiview.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Code editor theme preset shared by Desktop Swing and Android Mobile LMS.
 * Provides a small set of popular-looking themes (VSCode-like, Monokai-like, etc.).
 */
public class CodeTheme implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String id;
    public final String name;
    public final int background;
    public final int plainText;
    public final int tag;
    public final int tagName;
    public final int attributeName;
    public final int attributeValue;
    public final int comment;
    public final int punctuation;
    public final int doctype;

    public CodeTheme(String id, String name,
                     int background, int plainText,
                     int tag, int tagName, int attributeName,
                     int attributeValue, int comment,
                     int punctuation, int doctype) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.background = background;
        this.plainText = plainText;
        this.tag = tag;
        this.tagName = tagName;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.comment = comment;
        this.punctuation = punctuation;
        this.doctype = doctype;
    }

    public static List<CodeTheme> all() {
        List<CodeTheme> list = new ArrayList<>();

        // 1) DigiView Dark (default) - inspired by modern dark editors
        list.add(new CodeTheme(
            "digiview",
            "DigiView Dark",
            0xFF0B1526, 0xFFE5EDF7,
            0xFF7DD3FC, 0xFF38BDF8, 0xFF93C5FD,
            0xFFA7F3D0, 0xFF64748B, 0xFF94A3B8, 0xFFF0ABFC
        ));

        // 2) VS Code Dark+
        list.add(new CodeTheme(
            "vscode",
            "VS Code Dark+",
            0xFF1E1E1E, 0xFFD4D4D4,
            0xFF569CD6, 0xFF569CD6, 0xFF9CDCFE,
            0xFFCE9178, 0xFF6A9955, 0xFFD4D4D4, 0xFFD7BA7D
        ));

        // 3) Monokai
        list.add(new CodeTheme(
            "monokai",
            "Monokai",
            0xFF272822, 0xFFF8F8F2,
            0xFFF92672, 0xFFF92672, 0xFFA6E22E,
            0xFFE6DB74, 0xFF75715E, 0xFFF8F8F2, 0xFFAE81FF
        ));

        // 4) GitHub Light
        list.add(new CodeTheme(
            "github_light",
            "GitHub Light",
            0xFFFAFBFC, 0xFF24292E,
            0xFF005CC5, 0xFF005CC5, 0xFF6F42C1,
            0xFF032F62, 0xFF6A737D, 0xFF24292E, 0xFFE36209
        ));

        // 5) Solarized Dark
        list.add(new CodeTheme(
            "solarized_dark",
            "Solarized Dark",
            0xFF002B36, 0xFF93A1A1,
            0xFF268BD2, 0xFF268BD2, 0xFF93A1A1,
            0xFF2AA198, 0xFF586E75, 0xFF93A1A1, 0xFFCB4B16
        ));

        return Collections.unmodifiableList(list);
    }

    public static Map<String, CodeTheme> map() {
        Map<String, CodeTheme> map = new LinkedHashMap<>();
        for (CodeTheme t : all()) map.put(t.id, t);
        return map;
    }

    public static CodeTheme getById(String id) {
        Map<String, CodeTheme> map = map();
        if (id == null || id.trim().isEmpty()) return map.get("digiview");
        CodeTheme t = map.get(id.toLowerCase());
        return t != null ? t : map.get("digiview");
    }
}

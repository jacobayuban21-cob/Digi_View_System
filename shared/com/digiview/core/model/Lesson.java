package com.digiview.core.model;

import java.io.Serializable;

/**
 * Universal Lesson Curriculum Blueprint shared by Desktop Swing and Android Mobile LMS.
 */
public class Lesson implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String title;
    public final String icon;
    public final String explanation;
    public final String code;

    public Lesson(String title, String icon, String explanation, String code) {
        this.title = title != null ? title : "";
        this.icon = icon != null ? icon : "📘";
        this.explanation = explanation != null ? explanation : "";
        this.code = code != null ? code.replace("\\n", "\n") : "";
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getCode() {
        return code;
    }
}

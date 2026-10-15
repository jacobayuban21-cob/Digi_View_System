package com.digiview.core.model;

import java.io.Serializable;

/**
 * Universal Achievement Blueprint shared by Desktop Swing and Android Mobile.
 */
public class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String icon;
    public final String title;
    public final String description;
    public final int currentProgress;
    public final int targetProgress;

    public Achievement(String icon, String title, String description, int currentProgress, int targetProgress) {
        this.icon = icon != null ? icon : "🏆";
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.currentProgress = currentProgress;
        this.targetProgress = targetProgress > 0 ? targetProgress : 1;
    }

    public boolean isUnlocked() {
        return currentProgress >= targetProgress;
    }

    public int getProgressPercent() {
        if (targetProgress <= 0) return 100;
        return Math.min(100, Math.max(0, (int) Math.round((currentProgress * 100.0) / targetProgress)));
    }
}

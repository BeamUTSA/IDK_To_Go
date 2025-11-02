package com.idktogo.idk_to_go.core;

import com.idktogo.idk_to_go.data.AppStorage;
import javafx.scene.Scene;

import java.net.URL;
import java.util.Objects;

public final class ThemeManager {
    private ThemeManager() {}

    public enum Theme { LIGHT, DARK }

    // Classpath locations
    private static final String BASE = "/com/idktogo/idk_to_go/styles/";
    private static final String GLOBAL = BASE + "global.css";
    private static final String LIGHT  = BASE + "global-light.css";
    private static final String DARK   = BASE + "global-dark.css";

    private static final String KEY = "appearance.theme"; // persisted as "LIGHT" or "DARK"

    public static Theme getActiveTheme() {
        String saved = AppStorage.load(KEY);
        if ("DARK".equalsIgnoreCase(saved)) return Theme.DARK;
        return Theme.LIGHT;
    }

    public static void setActiveTheme(Theme theme) {
        AppStorage.save(KEY, theme.name());
    }

    public static Theme toggleTheme() {
        Theme next = (getActiveTheme() == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;
        setActiveTheme(next);
        return next;
    }

    /** Apply base + active theme to the given scene (idempotent). */
    public static void applyTheme(Scene scene) {
        Objects.requireNonNull(scene, "scene");
        scene.getStylesheets().removeIf(s ->
                s.endsWith("global.css") || s.endsWith("global-light.css") || s.endsWith("global-dark.css"));

        scene.getStylesheets().add(require(GLOBAL).toExternalForm());
        if (getActiveTheme() == Theme.DARK) {
            scene.getStylesheets().add(require(DARK).toExternalForm());
        } else {
            scene.getStylesheets().add(require(LIGHT).toExternalForm());
        }
        System.out.println(scene.getStylesheets());
    }

    private static URL require(String path) {
        URL url = ThemeManager.class.getResource(path);
        if (url == null) throw new IllegalStateException("Missing stylesheet: " + path);
        return url;
    }
}

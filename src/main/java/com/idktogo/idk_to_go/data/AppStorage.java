package com.idktogo.idk_to_go.data;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.prefs.Preferences;

/**
 * Manages local file operations for session logs and user preferences.
 */
public class AppStorage {
    private static final String FILE_PATH = "data/history.txt";
    private static final Preferences prefs = Preferences.userNodeForPackage(AppStorage.class);

    // Ensure the data directory and history file exist
    private static void ensureFileExists() throws IOException {
        Path dir = Paths.get("data");
        if (!Files.exists(dir)) Files.createDirectories(dir);

        Path file = Paths.get(FILE_PATH);
        if (!Files.exists(file)) Files.createFile(file);
    }

    // Append a new session message to history.txt
    public static void saveSession(String content) {
        try {
            ensureFileExists();
            try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
                writer.write(content + System.lineSeparator());
            }
            System.out.println("Session saved: " + content);
        } catch (IOException e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }

    private static int currentUserId = -1;
    private static String currentUsername = null;

    public static void setCurrentUserId(int id) { currentUserId = id; }
    public static int getCurrentUserId() { return currentUserId; }

    public static void setCurrentUsername(String name) { currentUsername = name; }
    public static String getCurrentUsername() { return currentUsername; }

    public static void clearUser() {
        currentUserId = -1;
        currentUsername = null;
    }

    // Save a key-value pair
    public static void save(String key, String value) {
        try {
            prefs.put(key, value);
            System.out.println("Saved key '" + key + "' with value: " + value);
        } catch (Exception e) {
            System.err.println("Error saving key " + key + ": " + e.getMessage());
        }
    }

    // Load a saved value
    public static String load(String key) {
        return prefs.get(key, null);
    }

    // Remove a saved key
    public static void remove(String key) {
        try {
            prefs.remove(key);
            System.out.println("Removed key '" + key + "' from storage.");
        } catch (Exception e) {
            System.err.println("Error removing key " + key + ": " + e.getMessage());
        }
    }

    // Clear all stored preferences
    public static void clearAll() {
        try {
            prefs.clear();
            System.out.println("All AppStorage data cleared.");
        } catch (Exception e) {
            System.err.println("Error clearing AppStorage: " + e.getMessage());
        }
    }
}

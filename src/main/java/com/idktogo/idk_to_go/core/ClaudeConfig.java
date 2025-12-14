package com.idktogo.idk_to_go.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads the Claude API key from a configuration file.
 * This file should not be committed to version control.
 * PLEASE DO NOT FLOOD OUR TOKENS :(
 */
public class ClaudeConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_PROPERTY = "claude.api.key";
    private static final String USER_CONFIG_DIR = ".idktogo";

    private static String cachedApiKey = null;

    private ClaudeConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static String getApiKey() {
        if (cachedApiKey != null) {
            return cachedApiKey;
        }

        String systemKey = System.getProperty(KEY_PROPERTY);
        if (systemKey != null && !systemKey.isEmpty()) {
            cachedApiKey = systemKey;
            System.out.println("Loaded API key from system property");
            return cachedApiKey;
        }

        String envKey = System.getenv("CLAUDE_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            cachedApiKey = envKey;
            System.out.println("Loaded API key from environment variable");
            return cachedApiKey;
        }

        String projectKey = loadFromFile(CONFIG_FILE);
        if (projectKey != null) {
            cachedApiKey = projectKey;
            System.out.println("Loaded API key from project config file");
            return cachedApiKey;
        }

        String userKey = loadFromUserHome();
        if (userKey != null) {
            cachedApiKey = userKey;
            System.out.println("Loaded API key from user home config file");
            return cachedApiKey;
        }

        throw new IllegalStateException(
                "Claude API key not found!\n\n" +
                        "Please set it via system property, environment variable, or in a config.properties file."
        );
    }

    private static String loadFromFile(String filename) {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(filename)) {
            props.load(input);
            String key = props.getProperty(KEY_PROPERTY);
            if (key != null && !key.trim().isEmpty()) {
                return key.trim();
            }
        } catch (FileNotFoundException e) {
            // File not found, try next location
        } catch (IOException e) {
            System.err.println("Error reading config file: " + filename);
            e.printStackTrace();
        }
        return null;
    }

    private static String loadFromUserHome() {
        String userHome = System.getProperty("user.home");
        Path configPath = Paths.get(userHome, USER_CONFIG_DIR, CONFIG_FILE);

        if (Files.exists(configPath)) {
            return loadFromFile(configPath.toString());
        }
        return null;
    }

    public static boolean isConfigured() {
        try {
            String key = getApiKey();
            return key != null && !key.isEmpty();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static void createConfigFile(String apiKey) throws IOException {
        Properties props = new Properties();
        props.setProperty(KEY_PROPERTY, apiKey);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "Claude Configuration - DO NOT COMMIT THIS FILE!");
            System.out.println("Created config file: " + CONFIG_FILE);
        }
    }

    public static void createUserConfigFile(String apiKey) throws IOException {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, USER_CONFIG_DIR);

        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        Path configPath = configDir.resolve(CONFIG_FILE);
        Properties props = new Properties();
        props.setProperty(KEY_PROPERTY, apiKey);

        try (OutputStream output = Files.newOutputStream(configPath)) {
            props.store(output, "Claude Configuration");
            System.out.println("Created user config file: " + configPath);
        }
    }

    public static void clearCache() {
        cachedApiKey = null;
    }
}

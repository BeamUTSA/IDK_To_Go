package com.idktogo.idk_to_go.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads the OpenAI API key from a configuration file.
 * This file should not be committed to version control.
 * PLEASE DO NOT FLOOD OUR TOKENS :(
 */
public class OpenAiConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_PROPERTY = "openai.api.key";
    private static final String USER_CONFIG_DIR = ".idktogo";

    private static String cachedApiKey = null;

    private OpenAiConfig() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves the OpenAI API key.
     * The key is loaded from system properties, project config, or user home config.
     * It's cached after the first successful load.
     *
     * @return The API key.
     * @throws IllegalStateException if no API key is found.
     */
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

        String envKey = System.getenv("OPENAI_API_KEY");
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
                "OpenAI API key not found!\n\n" +
                        "Please set it via system property, environment variable, or in a config.properties file."
        );
    }

    /**
     * Loads the API key from a properties file.
     *
     * @param filename Path to the properties file.
     * @return The API key, or null if not found.
     */
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

    /**
     * Loads the API key from the user's home directory config file.
     *
     * @return The API key, or null if not found.
     */
    private static String loadFromUserHome() {
        String userHome = System.getProperty("user.home");
        Path configPath = Paths.get(userHome, USER_CONFIG_DIR, CONFIG_FILE);

        if (Files.exists(configPath)) {
            return loadFromFile(configPath.toString());
        }
        return null;
    }

    /**
     * Checks if an API key is configured.
     *
     * @return True if a valid API key is available.
     */
    public static boolean isConfigured() {
        try {
            String key = getApiKey();
            return key != null && !key.isEmpty();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Creates a sample config.properties file in the project root.
     *
     * @param apiKey The API key to save.
     * @throws IOException if the file cannot be written.
     */
    public static void createConfigFile(String apiKey) throws IOException {
        Properties props = new Properties();
        props.setProperty(KEY_PROPERTY, apiKey);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "OpenAI Configuration - DO NOT COMMIT THIS FILE!");
            System.out.println("Created config file: " + CONFIG_FILE);
        }
    }

    /**
     * Creates a config file in the user's home directory.
     *
     * @param apiKey The API key to save.
     * @throws IOException if the file cannot be written.
     */
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
            props.store(output, "OpenAI Configuration");
            System.out.println("Created user config file: " + configPath);
        }
    }

    /**
     * Clears the cached API key.
     */
    public static void clearCache() {
        cachedApiKey = null;
    }
}

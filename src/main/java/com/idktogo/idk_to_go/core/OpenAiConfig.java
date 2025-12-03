package com.idktogo.idk_to_go.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Secure configuration loader for OpenAI API.
 *
 * This class loads the API key from an external configuration file that should
 * NOT be committed to version control.
 *
 * Setup Instructions:
 * 1. Create a file named "config.properties" in your project root
 * 2. Add this line: openai.api.key=your_actual_key_here
 * 3. Add "config.properties" to your .gitignore file
 * 4. For deployment, ensure each device has its own config.properties file
 *
 * Alternative locations checked (in order):
 * 1. ./config.properties (project root)
 * 2. ~/.idktogo/config.properties (user home directory)
 * 3. System property: -Dopenai.api.key=your_key
 */
public class OpenAiConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_PROPERTY = "openai.api.key";
    private static final String USER_CONFIG_DIR = ".idktogo";

    private static String cachedApiKey = null;

    private OpenAiConfig() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }

    /**
     * Gets the OpenAI API key from the first available source:
     * 1. System property (-Dopenai.api.key=...)
     * 2. config.properties in project root
     * 3. config.properties in user home directory (~/.idktogo/)
     *
     * The key is cached after first load for performance.
     *
     * @return The API key
     * @throws IllegalStateException if no valid API key is found
     */
    public static String getApiKey() {
        // Return cached key if available
        if (cachedApiKey != null) {
            return cachedApiKey;
        }

        // Try system property first (for testing/development)
        String systemKey = System.getProperty(KEY_PROPERTY);
        if (systemKey != null && !systemKey.isEmpty()) {
            cachedApiKey = systemKey;
            System.out.println("Loaded API key from system property");
            return cachedApiKey;
        }

        // Try environment variable (backwards compatibility)
        String envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            cachedApiKey = envKey;
            System.out.println("Loaded API key from environment variable");
            return cachedApiKey;
        }

        // Try project root config file
        String projectKey = loadFromFile(CONFIG_FILE);
        if (projectKey != null) {
            cachedApiKey = projectKey;
            System.out.println("Loaded API key from project config file");
            return cachedApiKey;
        }

        // Try user home directory config file
        String userKey = loadFromUserHome();
        if (userKey != null) {
            cachedApiKey = userKey;
            System.out.println("Loaded API key from user home config file");
            return cachedApiKey;
        }

        // No key found anywhere
        throw new IllegalStateException(
                "OpenAI API key not found!\n\n" +
                        "Please create a config.properties file with:\n" +
                        "openai.api.key=your_key_here\n\n" +
                        "Checked locations:\n" +
                        "1. System property: -D" + KEY_PROPERTY + "\n" +
                        "2. Project root: ./" + CONFIG_FILE + "\n" +
                        "3. User home: ~/" + USER_CONFIG_DIR + "/" + CONFIG_FILE
        );
    }

    /**
     * Loads the API key from a properties file at the given path.
     *
     * @param filename The path to the properties file
     * @return The API key, or null if not found or error occurred
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
            // File doesn't exist - this is normal, try next location
        } catch (IOException e) {
            System.err.println("Error reading config file: " + filename);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads the API key from the user's home directory.
     * Location: ~/.idktogo/config.properties
     *
     * @return The API key, or null if not found
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
     * Checks if the API key is configured without throwing an exception.
     *
     * @return true if a valid API key is available
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
     * Useful for first-time setup.
     *
     * @param apiKey The API key to save
     * @throws IOException if the file cannot be written
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
     * Location: ~/.idktogo/config.properties
     *
     * @param apiKey The API key to save
     * @throws IOException if the file cannot be written
     */
    public static void createUserConfigFile(String apiKey) throws IOException {
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, USER_CONFIG_DIR);

        // Create directory if it doesn't exist
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
     * Clears the cached API key. Useful for testing or if the config changes.
     */
    public static void clearCache() {
        cachedApiKey = null;
    }
}
package com.idktogo.idk_to_go.core;

public class OpenAiConfig {
    private static final String API_KEY = "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";
    private static final String apiKey = System.getenv(API_KEY);

    private OpenAiConfig() {}

    public static String getApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Missing environment variable: " + API_KEY);
        }
        return apiKey;
    }

}

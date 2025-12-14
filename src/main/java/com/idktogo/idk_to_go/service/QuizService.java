package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.core.ClaudeConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class QuizService {
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL = "claude-sonnet-4-20250514";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final HttpClient httpClient;
    private final String apiKey;

    public QuizService() {
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = ClaudeConfig.getApiKey();
    }

    public String sendMessage(String systemMessage, String userMessage, int maxTokens) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", CLAUDE_MODEL);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("system", systemMessage);

        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.put(userMsg);
        requestBody.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLAUDE_API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed: " + response.body());
        }

        JSONObject responseJson = new JSONObject(response.body());
        JSONArray content = responseJson.getJSONArray("content");
        if (content.length() > 0) {
            return content.getJSONObject(0).getString("text");
        }
        return "{}";
    }
}

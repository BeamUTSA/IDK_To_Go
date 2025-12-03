package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import javafx.fxml.FXML;
import java.util.List;

public class quizController {

    //private final OpenAIClient client;
    private final List<Restaurant> restaurants;

    public quizController(List<Restaurant> restaurants) {
        //this.client = OpenAIOkHttpClient.fromEnv();
        this.restaurants = restaurants;
    }

    public String getRecommendation(String cuisinePreference,
                                    String pricePreference,
                                    String dietaryNeeds,
                                    String mood) {

        String restaurantList = buildRestaurantListText();

        String systemPrompt = """
            You are a food recommendation assistant.
            The ONLY places you are allowed to recommend are from this list:
            %s

            When you answer, you MUST output valid JSON in this exact format:
            {
              "recommended_name": "...",
              "reason": "...",
              "alternatives": ["...", "..."]
            }

            Always pick the single best match from the list based on the user's preferences.
            If nothing is a good match, still pick the closest one and explain why.
            """.formatted(restaurantList);

        String userPrompt = """
            User preferences:
            - Cuisine wanted: %s
            - Price preference: %s
            - Dietary needs: %s
            - Current mood / craving: %s

            Based on these preferences, choose the best restaurant from the list.
            """.formatted(cuisinePreference, pricePreference, dietaryNeeds, mood);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_5_1)   // or another chat-capable model
                .addSystemMessage(systemPrompt)
                .addUserMessage(userPrompt)
                .maxTokens(300L)
                .temperature(0.4)           // lower = more deterministic
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        // Get the first choice text
        return completion.choices()
                .get(0)
                .message()
                .content()
                .orElse("");
    }
}
    /**
     * Return to the main screen.
     */
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }

    /**
     * Placeholder for future AI-based quiz generation.
     */
    @FXML
    private void generateQuiz() {
        System.out.println("Quiz generation triggered... (not implemented yet)");
        // TODO: Implement AI quiz generation using backend or cloud service
    }
}


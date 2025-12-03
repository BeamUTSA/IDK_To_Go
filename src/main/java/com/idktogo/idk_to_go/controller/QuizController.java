package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.Restaurant;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.json.JSONArray;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QuizController {

    @FXML private VBox quizContainer;
    @FXML private Button submitButton;
    @FXML private TextArea recommendationOutput;
    @FXML private Label quizStatusLabel;

    private OpenAIClient client;
    private List<Restaurant> allRestaurants;
    private final Map<String, TextField> questionAnswerMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        try {
            this.client = OpenAIOkHttpClient.fromEnv();
        } catch (Exception e) {
            System.err.println("Failed to initialize OpenAIClient: " + e.getMessage());
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Configuration Error");
                alert.setHeaderText("OpenAI API Key Not Found");
                alert.setContentText("The application could not initialize the AI service.\n\nPlease make sure you have set the 'OPENAI_API_KEY' environment variable in your run configuration and try again.");
                alert.showAndWait();
                quizStatusLabel.setText("Error: OpenAI API key not configured.");
            });
        }
        if (client != null) {
            loadAllRestaurantsAndGenerateQuiz();
        }
    }

    private void loadAllRestaurantsAndGenerateQuiz() {
        quizStatusLabel.setText("Loading restaurants...");
        RestaurantDAO.listAll()
                .thenAccept(restaurants -> {
                    this.allRestaurants = restaurants;
                    Platform.runLater(this::generateQuiz);
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> quizStatusLabel.setText("Error loading restaurants: " + ex.getMessage()));
                    System.err.println("Error loading restaurants: " + ex.getMessage());
                    return null;
                });
    }

    private void generateQuiz() {
        if (client == null) {
            quizStatusLabel.setText("OpenAI client not initialized.");
            return;
        }

        quizStatusLabel.setText("Generating quiz questions...");

        CompletableFuture.supplyAsync(() -> {
            String prompt = """
                    Generate a JSON array of 3-5 creative and engaging questions to ask a user to determine their food preferences.
                    The output MUST be a valid JSON array of strings.
                    Example: ["What's your go-to comfort food?", "Spicy or mild?"]
                    """;

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_5)
                    .addSystemMessage("You are a helpful assistant that generates quiz questions.")
                    .addUserMessage(prompt)
                    .maxTokens(200L)
                    .temperature(0.7)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);
            return completion.choices().get(0).message().content().orElse("[]");
        }).thenAccept(jsonResponse -> Platform.runLater(() -> {
            try {
                JSONArray questions = new JSONArray(jsonResponse);
                quizContainer.getChildren().clear();
                questionAnswerMap.clear();

                for (int i = 0; i < questions.length(); i++) {
                    String questionText = questions.getString(i);
                    Label questionLabel = new Label(questionText);
                    TextField answerField = new TextField();
                    answerField.setPromptText("Your answer...");
                    quizContainer.getChildren().addAll(questionLabel, answerField);
                    questionAnswerMap.put(questionText, answerField);
                }
                submitButton.setVisible(true);
                quizStatusLabel.setText("Quiz ready!");
            } catch (Exception e) {
                quizStatusLabel.setText("Failed to generate quiz. Please try again.");
                System.err.println("Error parsing quiz JSON: " + e.getMessage());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> quizStatusLabel.setText("Error generating quiz: " + ex.getMessage()));
            System.err.println("Error generating quiz: " + ex.getMessage());
            return null;
        });
    }

    private String buildRestaurantListText() {
        if (allRestaurants == null || allRestaurants.isEmpty()) {
            return "No restaurants available.";
        }
        return allRestaurants.stream()
                .map(r -> String.format("- Name: %s, Category: %s, Location: %s", r.name(), r.category(), r.location()))
                .collect(Collectors.joining("\n"));
    }

    @FXML
    private void generateRecommendation() {
        if (client == null || allRestaurants == null || allRestaurants.isEmpty()) {
            recommendationOutput.setText("System not ready. Please wait or restart.");
            return;
        }

        StringBuilder preferences = new StringBuilder("User preferences:\n");
        questionAnswerMap.forEach((question, answerField) -> {
            String answer = answerField.getText().trim().intern();
            if (!answer.isEmpty()) {
                preferences.append("- ").append(question).append(": ").append(answer).append("\n");
            }
        });

        quizStatusLabel.setText("Generating recommendation...");
        recommendationOutput.setVisible(true);

        CompletableFuture.supplyAsync(() -> {
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

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_5)
                    .addSystemMessage(systemPrompt)
                    .addUserMessage(preferences.toString())
                    .maxTokens(300L)
                    .temperature(0.4)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);
            return completion.choices().get(0).message().content().orElse("No recommendation found.");
        }).thenAccept(recommendation -> Platform.runLater(() -> {
            recommendationOutput.setText(recommendation);
            quizStatusLabel.setText("Recommendation generated!");
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                recommendationOutput.setText("Error generating recommendation: " + ex.getMessage());
                quizStatusLabel.setText("Error.");
            });
            System.err.println("Error generating recommendation: " + ex.getMessage());
            return null;
        });
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }
}

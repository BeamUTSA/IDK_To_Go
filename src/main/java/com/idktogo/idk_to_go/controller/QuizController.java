package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.OpenAiConfig;
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
import org.json.JSONObject;

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
            // Load API key from config file and build the client
            String apiKey = OpenAiConfig.getApiKey();
            this.client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
            System.out.println("OpenAI client initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize OpenAIClient: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Configuration Error");
                alert.setHeaderText("OpenAI API Key Not Found");
                alert.setContentText("The application could not initialize the AI service.\n\n" +
                        "Please create a config.properties file in your project root with:\n" +
                        "openai.api.key=your_actual_key_here\n\n" +
                        "Make sure to add config.properties to your .gitignore!\n\n" +
                        "Error: " + e.getMessage());
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
                    System.out.println("Loaded " + restaurants.size() + " restaurants");
                    Platform.runLater(this::generateQuiz);
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        quizStatusLabel.setText("Error loading restaurants: " + ex.getMessage());
                        showError("Database Error", "Could not load restaurants", ex.getMessage());
                    });
                    ex.printStackTrace();
                    return null;
                });
    }

    private void generateQuiz() {
        if (client == null) {
            quizStatusLabel.setText("OpenAI client not initialized.");
            return;
        }

        quizStatusLabel.setText("Generating quiz questions...");
        submitButton.setDisable(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                // Updated prompt for 5-8 questions with clear JSON format
                String prompt = """
                        Generate a JSON array of exactly 5-8 creative, fun, and quirky questions to determine 
                        what a user should eat. Make the questions engaging and varied.
                        
                        Include questions about:
                        - Mood/feeling
                        - Flavor preferences (spicy, sweet, savory)
                        - Texture preferences
                        - Dietary restrictions or preferences
                        - Cuisine types they're craving
                        - Eating context (comfort food, adventure, health-conscious)
                        
                        Output ONLY a valid JSON array of strings, nothing else.
                        Example format: ["What's your vibe today?", "Spicy or mild?", "Comfort or adventure?"]
                        """;

                // Use GPT-4 (GPT-5 doesn't exist yet)
                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model(ChatModel.GPT_4) // GPT-4 Omni
                        .addSystemMessage("You are a creative quiz generator. Output only valid JSON arrays.")
                        .addUserMessage(prompt)
                        .maxTokens(300L)
                        .temperature(0.9) // Higher temperature for more creative questions
                        .build();

                System.out.println("Sending request to OpenAI for quiz generation...");
                ChatCompletion completion = client.chat().completions().create(params);
                String response = completion.choices().get(0).message().content().orElse("[]");
                System.out.println("Received response: " + response);

                return response;

            } catch (Exception e) {
                System.err.println("Error in quiz generation: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to generate quiz: " + e.getMessage(), e);
            }
        }).thenAccept(jsonResponse -> Platform.runLater(() -> {
            try {
                // Clean the response - sometimes GPT adds markdown code blocks
                String cleanedResponse = jsonResponse.trim();
                if (cleanedResponse.startsWith("```json")) {
                    cleanedResponse = cleanedResponse.substring(7);
                }
                if (cleanedResponse.startsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(3);
                }
                if (cleanedResponse.endsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
                }
                cleanedResponse = cleanedResponse.trim();

                System.out.println("Parsing JSON: " + cleanedResponse);
                JSONArray questions = new JSONArray(cleanedResponse);

                // Validate we got the right number of questions
                if (questions.length() < 5 || questions.length() > 8) {
                    throw new Exception("Expected 5-8 questions, got " + questions.length());
                }

                quizContainer.getChildren().clear();
                questionAnswerMap.clear();

                for (int i = 0; i < questions.length(); i++) {
                    String questionText = questions.getString(i);

                    Label questionLabel = new Label((i + 1) + ". " + questionText);
                    questionLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");

                    TextField answerField = new TextField();
                    answerField.setPromptText("Your answer...");
                    answerField.setPrefWidth(400);

                    quizContainer.getChildren().addAll(questionLabel, answerField);
                    questionAnswerMap.put(questionText, answerField);
                }

                submitButton.setVisible(true);
                submitButton.setDisable(false);
                quizStatusLabel.setText("Quiz ready! Answer the questions and click Submit.");

            } catch (Exception e) {
                quizStatusLabel.setText("Failed to parse quiz questions.");
                System.err.println("Error parsing quiz JSON: " + e.getMessage());
                e.printStackTrace();
                showError("Quiz Generation Error",
                        "Could not parse quiz questions",
                        "The AI returned an invalid format. Please try again.");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                quizStatusLabel.setText("Error generating quiz.");
                submitButton.setDisable(false);
                showError("Quiz Generation Error",
                        "Could not generate quiz",
                        ex.getMessage());
            });
            ex.printStackTrace();
            return null;
        });
    }

    private String buildRestaurantListText() {
        if (allRestaurants == null || allRestaurants.isEmpty()) {
            return "No restaurants available.";
        }
        return allRestaurants.stream()
                .map(r -> String.format("- %s (Category: %s, Location: %s)",
                        r.name(), r.category(), r.location()))
                .collect(Collectors.joining("\n"));
    }

    @FXML
    private void generateRecommendation() {
        if (client == null) {
            showError("Error", "System not ready", "OpenAI client is not initialized.");
            return;
        }

        if (allRestaurants == null || allRestaurants.isEmpty()) {
            showError("Error", "No restaurants available", "Please check your database connection.");
            return;
        }

        // Collect user answers
        StringBuilder preferences = new StringBuilder("User preferences:\n");
        int answeredCount = 0;

        for (Map.Entry<String, TextField> entry : questionAnswerMap.entrySet()) {
            String answer = entry.getValue().getText().trim();
            if (!answer.isEmpty()) {
                preferences.append("- ").append(entry.getKey()).append(": ").append(answer).append("\n");
                answeredCount++;
            }
        }

        // Validate that user answered at least some questions
        if (answeredCount == 0) {
            showError("No Answers",
                    "Please answer at least one question",
                    "We need your preferences to make a recommendation!");
            return;
        }

        quizStatusLabel.setText("Generating recommendation...");
        recommendationOutput.setVisible(true);
        recommendationOutput.setText("Thinking...");
        submitButton.setDisable(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                String restaurantList = buildRestaurantListText();

                String systemPrompt = String.format("""
                        You are a helpful food recommendation assistant.
                        
                        AVAILABLE RESTAURANTS:
                        %s
                        
                        RULES:
                        1. You MUST ONLY recommend restaurants from the list above
                        2. Choose the single best match based on user preferences
                        3. If no perfect match exists, pick the closest option and explain why
                        4. Output ONLY valid JSON in this exact format:
                        {
                          "recommended_name": "exact restaurant name from list",
                          "reason": "2-3 sentence explanation of why this matches their preferences",
                          "alternatives": ["alternative 1 name", "alternative 2 name"]
                        }
                        
                        Do not include any text outside the JSON object.
                        """, restaurantList);

                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model(ChatModel.GPT_4) // GPT-4 Omni
                        .addSystemMessage(systemPrompt)
                        .addUserMessage(preferences.toString())
                        .maxTokens(400L)
                        .temperature(0.5) // Moderate creativity, but stay focused
                        .build();

                System.out.println("Sending request to OpenAI for recommendation...");
                ChatCompletion completion = client.chat().completions().create(params);
                String response = completion.choices().get(0).message().content().orElse("{}");
                System.out.println("Received recommendation: " + response);

                return response;

            } catch (Exception e) {
                System.err.println("Error generating recommendation: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to generate recommendation: " + e.getMessage(), e);
            }
        }).thenAccept(recommendationJson -> Platform.runLater(() -> {
            try {
                // Clean the response
                String cleanedResponse = recommendationJson.trim();
                if (cleanedResponse.startsWith("```json")) {
                    cleanedResponse = cleanedResponse.substring(7);
                }
                if (cleanedResponse.startsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(3);
                }
                if (cleanedResponse.endsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
                }
                cleanedResponse = cleanedResponse.trim();

                // Parse the JSON response
                JSONObject recommendation = new JSONObject(cleanedResponse);

                String restaurantName = recommendation.getString("recommended_name");
                String reason = recommendation.getString("reason");
                JSONArray alternatives = recommendation.getJSONArray("alternatives");

                // Format the output nicely
                StringBuilder output = new StringBuilder();
                output.append("🍽️ RECOMMENDATION 🍽️\n\n");
                output.append("We recommend: ").append(restaurantName).append("\n\n");
                output.append("Why? ").append(reason).append("\n\n");
                output.append("Other options:\n");
                for (int i = 0; i < alternatives.length(); i++) {
                    output.append("  • ").append(alternatives.getString(i)).append("\n");
                }

                recommendationOutput.setText(output.toString());
                quizStatusLabel.setText("Recommendation ready!");
                submitButton.setDisable(false);

            } catch (Exception e) {
                System.err.println("Error parsing recommendation JSON: " + e.getMessage());
                e.printStackTrace();
                // Show raw response if parsing fails
                recommendationOutput.setText("Recommendation:\n\n" + recommendationJson);
                quizStatusLabel.setText("Recommendation generated (raw format)");
                submitButton.setDisable(false);
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                recommendationOutput.setText("Error: " + ex.getMessage() +
                        "\n\nPlease try again or check your API connection.");
                quizStatusLabel.setText("Error generating recommendation.");
                submitButton.setDisable(false);
            });
            ex.printStackTrace();
            return null;
        });
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }
}
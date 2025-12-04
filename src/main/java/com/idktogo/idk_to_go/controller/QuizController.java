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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QuizController {

    @FXML private StackPane rootStackPane;
    @FXML private BorderPane mainContent;
    @FXML private VBox quizContainer;
    @FXML private Button submitButton;
    @FXML private Label quizStatusLabel;
    @FXML private VBox overlayPane;
    @FXML private VBox recommendationCard;

    private OpenAIClient client;
    private List<Restaurant> allRestaurants;
    private final Map<String, String> questionAnswerMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        try {
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

        quizStatusLabel.setText("Generating your personalized quiz...");
        submitButton.setDisable(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = """
                        Generate a JSON array of exactly 6 creative, fun, and quirky multiple-choice questions 
                        to determine what a user should eat. Each question should have 3-4 answer options.
                        
                        Format as an array of objects:
                        [
                          {
                            "question": "What's your vibe today?",
                            "options": ["Energetic", "Chill", "Adventurous", "Cozy"]
                          },
                          ...
                        ]
                        
                        Include varied questions about mood, flavor preferences, textures, cuisine types, 
                        eating context, and adventurousness.
                        
                        Make options concise (1-2 words) and varied (3-4 options per question).
                        Output ONLY valid JSON, nothing else.
                        """;

                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model(ChatModel.GPT_4)
                        .addSystemMessage("You are a creative quiz generator. Output only valid JSON.")
                        .addUserMessage(prompt)
                        .maxTokens(800L)
                        .temperature(0.9)
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
                String cleanedResponse = cleanJsonResponse(jsonResponse);
                System.out.println("Parsing JSON: " + cleanedResponse);
                JSONArray questions = new JSONArray(cleanedResponse);

                if (questions.length() < 5 || questions.length() > 8) {
                    throw new Exception("Expected 5-8 questions, got " + questions.length());
                }

                quizContainer.getChildren().clear();
                questionAnswerMap.clear();

                for (int i = 0; i < questions.length(); i++) {
                    JSONObject questionObj = questions.getJSONObject(i);
                    String questionText = questionObj.getString("question");
                    JSONArray options = questionObj.getJSONArray("options");

                    VBox questionBox = createQuestionCard(i + 1, questionText, options);
                    quizContainer.getChildren().add(questionBox);
                }

                submitButton.setVisible(true);
                submitButton.setDisable(false);
                submitButton.setText("Find My Perfect Restaurant");
                submitButton.setStyle("-fx-background-color: #0BBFFF; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 15 30; -fx-background-radius: 10; -fx-cursor: hand;");
                quizStatusLabel.setText("Answer the questions and discover your match!");

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

    // Creates a question card with button options
    private VBox createQuestionCard(int number, String question, JSONArray options) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label questionLabel = new Label(number + ". " + question);
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-text-fill: #2c3e50;");

        ToggleGroup toggleGroup = new ToggleGroup();
        VBox optionsBox = new VBox(8);

        for (int i = 0; i < options.length(); i++) {
            String optionText = options.getString(i);

            ToggleButton optionButton = new ToggleButton(optionText);
            optionButton.setToggleGroup(toggleGroup);
            optionButton.setMaxWidth(Double.MAX_VALUE);
            optionButton.setFont(Font.font("System", 14));
            optionButton.setStyle(
                    "-fx-background-color: #f8f9fa;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 12 20;" +
                            "-fx-border-color: #dee2e6;" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-width: 2;" +
                            "-fx-cursor: hand;"
            );

            // Hover effect
            optionButton.setOnMouseEntered(e -> {
                if (!optionButton.isSelected()) {
                    optionButton.setStyle(
                            "-fx-background-color: #e9ecef;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-border-color: #adb5bd;" +
                                    "-fx-border-radius: 8;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-cursor: hand;"
                    );
                }
            });

            optionButton.setOnMouseExited(e -> {
                if (!optionButton.isSelected()) {
                    optionButton.setStyle(
                            "-fx-background-color: #f8f9fa;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-border-color: #dee2e6;" +
                                    "-fx-border-radius: 8;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-cursor: hand;"
                    );
                }
            });

            // Selected style
            optionButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    optionButton.setStyle(
                            "-fx-background-color: #0BBFFF;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-border-color: #0BCAEE;" +
                                    "-fx-border-radius: 8;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-font-weight: bold;"
                    );
                    questionAnswerMap.put(question, optionText);
                } else {
                    optionButton.setStyle(
                            "-fx-background-color: #f8f9fa;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-border-color: #dee2e6;" +
                                    "-fx-border-radius: 8;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-cursor: hand;"
                    );
                }
            });

            optionsBox.getChildren().add(optionButton);
        }

        card.getChildren().addAll(questionLabel, optionsBox);
        return card;
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

        int totalQuestions = quizContainer.getChildren().size();
        int answeredCount = questionAnswerMap.size();

        if (answeredCount == 0) {
            showError("No Answers",
                    "Please answer at least one question",
                    "We need your preferences to make a recommendation!");
            return;
        }

        if (answeredCount < totalQuestions) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Incomplete Quiz");
            confirm.setHeaderText("You haven't answered all questions");
            confirm.setContentText("Answered: " + answeredCount + "/" + totalQuestions + "\n\n" +
                    "Continue anyway? (Results may be less accurate)");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        StringBuilder preferences = new StringBuilder("User preferences:\n");
        questionAnswerMap.forEach((question, answer) ->
                preferences.append("- ").append(question).append(": ").append(answer).append("\n"));

        quizStatusLabel.setText("Analyzing your preferences...");
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
                        .model(ChatModel.GPT_4)
                        .addSystemMessage(systemPrompt)
                        .addUserMessage(preferences.toString())
                        .maxTokens(400L)
                        .temperature(0.5)
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
                String cleanedResponse = cleanJsonResponse(recommendationJson);
                JSONObject recommendation = new JSONObject(cleanedResponse);

                String restaurantName = recommendation.getString("recommended_name");
                String reason = recommendation.getString("reason");
                JSONArray alternatives = recommendation.getJSONArray("alternatives");

                Restaurant restaurant = allRestaurants.stream()
                        .filter(r -> r.name().equalsIgnoreCase(restaurantName))
                        .findFirst()
                        .orElse(null);

                quizStatusLabel.setText("Found your perfect match!");
                submitButton.setDisable(false);

                // Show the recommendation overlay
                showRecommendationOverlay(restaurant, reason, alternatives);

            } catch (Exception e) {
                System.err.println("Error parsing recommendation JSON: " + e.getMessage());
                e.printStackTrace();
                quizStatusLabel.setText("Recommendation generated (parsing error)");
                submitButton.setDisable(false);
                showError("Parsing Error", "Could not parse recommendation", e.getMessage());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                quizStatusLabel.setText("Error generating recommendation.");
                submitButton.setDisable(false);
                showError("Recommendation Error", "Could not generate recommendation", ex.getMessage());
            });
            ex.printStackTrace();
            return null;
        });
    }

    // Displays an overlay with the restaurant recommendation
    private void showRecommendationOverlay(Restaurant restaurant, String reason, JSONArray alternatives) {
        recommendationCard.getChildren().clear();

        // Close button
        Button closeButton = new Button("X");
        closeButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #999;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10;"
        );
        closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-text-fill: #333;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 50;"
        ));
        closeButton.setOnMouseExited(e -> closeButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #999;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10;"
        ));
        closeButton.setOnAction(e -> hideOverlay());

        HBox closeBox = new HBox(closeButton);
        closeBox.setAlignment(Pos.TOP_RIGHT);

        Label titleLabel = new Label("Your Perfect Match!");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label restaurantLabel = new Label(restaurant != null ? restaurant.name() : "Restaurant");
        restaurantLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        restaurantLabel.setStyle("-fx-text-fill: #0BBFFF;");
        restaurantLabel.setWrapText(true);

        Label categoryLabel = new Label(restaurant != null ? "Category: " + restaurant.category() : "");
        categoryLabel.setFont(Font.font("System", 14));
        categoryLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Separator separator1 = new Separator();
        separator1.setPadding(new Insets(10, 0, 10, 0));

        Label reasonTitle = new Label("Why this matches you:");
        reasonTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        reasonTitle.setStyle("-fx-text-fill: #34495e;");

        Label reasonText = new Label(reason);
        reasonText.setFont(Font.font("System", 13));
        reasonText.setWrapText(true);
        reasonText.setStyle("-fx-text-fill: #555;");

        Label altTitle = new Label("Other great options:");
        altTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        altTitle.setStyle("-fx-text-fill: #34495e;");
        altTitle.setPadding(new Insets(10, 0, 0, 0));

        VBox altBox = new VBox(4);
        for (int i = 0; i < alternatives.length(); i++) {
            Label altLabel = new Label("- " + alternatives.getString(i));
            altLabel.setFont(Font.font("System", 12));
            altLabel.setStyle("-fx-text-fill: #666;");
            altBox.getChildren().add(altLabel);
        }

        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(10, 0, 10, 0));

        Button mapsButton = new Button("Open in Maps");
        mapsButton.setMaxWidth(Double.MAX_VALUE);
        mapsButton.setStyle(
                "-fx-background-color: #0BBFFF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        mapsButton.setOnMouseEntered(e -> mapsButton.setStyle(
                "-fx-background-color: #0BCEFF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        ));
        mapsButton.setOnMouseExited(e -> mapsButton.setStyle(
                "-fx-background-color: #0CCEFF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        ));
        mapsButton.setOnAction(e -> openInMaps(restaurant));

        Button retakeButton = new Button("Retake Quiz");
        retakeButton.setMaxWidth(Double.MAX_VALUE);
        retakeButton.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-text-fill: #555;" +
                        "-fx-font-size: 14;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        );
        retakeButton.setOnMouseEntered(e -> retakeButton.setStyle(
                "-fx-background-color: #e9ecef;" +
                        "-fx-text-fill: #333;" +
                        "-fx-font-size: 14;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #adb5bd;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        ));
        retakeButton.setOnMouseExited(e -> retakeButton.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-text-fill: #555;" +
                        "-fx-font-size: 14;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        ));
        retakeButton.setOnAction(e -> {
            hideOverlay();
            generateQuiz();
        });

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(mapsButton, retakeButton);

        recommendationCard.getChildren().addAll(
                closeBox,
                titleLabel,
                restaurantLabel,
                categoryLabel,
                separator1,
                reasonTitle,
                reasonText,
                altTitle,
                altBox,
                separator2,
                buttonBox
        );

        overlayPane.setVisible(true);
    }

    // Opens the restaurant location in Maps
    private void openInMaps(Restaurant restaurant) {
        if (restaurant != null && restaurant.location() != null && !restaurant.location().isBlank()) {
            try {
                Desktop.getDesktop().browse(new URI(restaurant.location()));
            } catch (Exception e) {
                System.err.println("Could not open maps URL: " + restaurant.location());
            }
        }
    }

    // Hides the overlay popup
    private void hideOverlay() {
        overlayPane.setVisible(false);
    }

    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
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

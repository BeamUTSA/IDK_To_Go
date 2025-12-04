package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.SessionManager;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.Restaurant;
import com.idktogo.idk_to_go.model.UserHistory;
import com.idktogo.idk_to_go.service.HistoryService;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.List;

public class MainController {

    @FXML private ImageView logoImageView;
    @FXML private Button optionsButton;
    @FXML private Button closestButton;
    @FXML private Button quizButton;
    @FXML private Button trendingButton;
    @FXML private Button hotButton;

    @FXML private Label hotRestaurant;
    @FXML private ImageView restaurantLogo;

    @FXML private ScrollPane historyScroll;
    @FXML private VBox historyBox;

    private final HistoryService historyService = new HistoryService();

    @FXML
    private void initialize() {
        loadHotRestaurant();
        loadHistoryList();
        setupLogoAnimation();
    }

    private void setupLogoAnimation() {
        Timeline shrinkAndGrow = new Timeline(
                new KeyFrame(Duration.seconds(0.1), new javafx.animation.KeyValue(logoImageView.scaleXProperty(), 0.9), new javafx.animation.KeyValue(logoImageView.scaleYProperty(), 0.9)),
                new KeyFrame(Duration.seconds(0.2), new javafx.animation.KeyValue(logoImageView.scaleXProperty(), 1.1), new javafx.animation.KeyValue(logoImageView.scaleYProperty(), 1.1)),
                new KeyFrame(Duration.seconds(0.3), new javafx.animation.KeyValue(logoImageView.scaleXProperty(), 1.0), new javafx.animation.KeyValue(logoImageView.scaleYProperty(), 1.0))
        );

        PauseTransition pause = new PauseTransition(Duration.seconds(5 + Math.random() * 5));
        pause.setOnFinished(event -> {
            shrinkAndGrow.play();
            pause.setDuration(Duration.seconds(5 + Math.random() * 5)); // Reset for next pause
            pause.play();
        });
        pause.play();
    }

    // Load the "hot" restaurant (top by weekly likes)
    private void loadHotRestaurant() {
        RestaurantDAO.topByWeeklyLikes(1)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    if (!restaurants.isEmpty()) {
                        displayHotRestaurant(restaurants.getFirst());
                    } else {
                        hotRestaurant.setText("No Hot Restaurant Yet");
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("Error loading hot restaurant: " + ex.getMessage());
                    Platform.runLater(() -> hotRestaurant.setText("Error loading hot restaurant"));
                    return null;
                });
    }

    // Display the hot restaurant's name and logo
    private void displayHotRestaurant(Restaurant restaurant) {
        hotRestaurant.setText(restaurant.name());
        String logoPath = restaurant.logo();

        if (logoPath != null && !logoPath.isBlank()) {
            if (!logoPath.startsWith("/")) logoPath = "/" + logoPath;
            try (InputStream imageStream = getClass().getResourceAsStream(logoPath)) {
                if (imageStream != null) {
                    restaurantLogo.setImage(new Image(imageStream));
                } else {
                    System.err.println("Logo not found on classpath: " + logoPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading restaurant logo: " + e.getMessage());
            }
        }
    }

    // Load and display user's interaction history
    private void loadHistoryList() {
        historyBox.getChildren().clear();

        Integer userId = SessionManager.getUserId();
        if (userId == null) {
            System.err.println("No user logged in.");
            return;
        }

        HistoryService.listByUser(userId)
                .thenAccept(historyList -> Platform.runLater(() -> {
                    historyBox.getChildren().clear();

                    if (historyList.isEmpty()) {
                        historyBox.getChildren().add(new Label("No history yet."));
                        return;
                    }

                    for (UserHistory entry : historyList) {
                        RestaurantDAO.findById(entry.restaurantId())
                                .thenAccept(optRestaurant -> optRestaurant.ifPresent(restaurant ->
                                        Platform.runLater(() -> {
                                            Button historyBtn = new Button(restaurant.name());
                                            historyBtn.getStyleClass().add("history-button");
                                            historyBtn.setOnAction(e -> Navigation.load(
                                                    "/com/idktogo/idk_to_go/restaurant.fxml",
                                                    controller -> ((RestaurantController) controller)
                                                            .setRestaurantId(restaurant.id())
                                            ));
                                            historyBox.getChildren().add(historyBtn);
                                        })
                                ))
                                .exceptionally(ex -> {
                                    System.err.println("Error loading restaurant from history: " + ex.getMessage());
                                    return null;
                                });
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("Error loading user history: " + ex.getMessage());
                    return null;
                });
    }

    // Navigate to the Closest Restaurants scene
    @FXML
    private void openClosest() {
        Navigation.load("/com/idktogo/idk_to_go/closest.fxml");
    }

    // Navigate to the AI Quiz scene
    @FXML
    private void openQuiz() {
        Navigation.load("/com/idktogo/idk_to_go/quiz.fxml");
    }

    // Navigate to the Trending Restaurants scene
    @FXML
    private void openTrending() {
        Navigation.load("/com/idktogo/idk_to_go/trending.fxml");
    }

    // Navigate to the Options scene
    @FXML
    private void openOptions() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }

    // Open the restaurant scene for the hot restaurant
    @FXML
    private void openRestaurantScene() {
        RestaurantDAO.topByWeeklyLikes(1)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    if (!restaurants.isEmpty()) {
                        Navigation.load("/com/idktogo/idk_to_go/restaurant.fxml",
                                controller -> ((RestaurantController) controller)
                                        .setRestaurantId(restaurants.getFirst().id()));
                    } else {
                        System.err.println("No hot restaurant found");
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("Error opening restaurant scene: " + ex.getMessage());
                    return null;
                });
    }

    // Open a URL in the default browser
    private void openUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            System.err.println("Could not open URL: " + e.getMessage());
        }
    }
}

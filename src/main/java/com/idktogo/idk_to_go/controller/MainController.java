package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.model.Restaurant;
import com.idktogo.idk_to_go.model.UserHistory;
import com.idktogo.idk_to_go.service.UserHistoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class MainController {

    // === FXML UI elements ===
    @FXML private Button optionsButton;
    @FXML private Button closestButton;
    @FXML private Button quizButton;
    @FXML private Button trendingButton;
    @FXML private Button hotButton;

    @FXML private Label hotRestaurant;
    @FXML private ImageView restaurantLogo;

    @FXML private ScrollPane historyScroll;
    @FXML private VBox historyBox;

    private final UserHistoryService historyService = UserHistoryService.getInstance();
    private final int userId = UserDAO.getLoggedInUserId();

    @FXML
    private void initialize() {
        loadHotRestaurant();
        loadHistoryList();
    }

    private void loadHotRestaurant() {
        RestaurantDAO.getTopRestaurantsByWeeklyLikes(1)
                .stream()
                .findFirst()
                .ifPresentOrElse(this::displayHotRestaurant, () -> hotRestaurant.setText("No Hot Restaurant Yet"));
    }

    private void displayHotRestaurant(Restaurant restaurant) {
        hotRestaurant.setText(restaurant.name());
        String logoPath = restaurant.logo();

        if (logoPath != null && !logoPath.isBlank()) {
            if (!logoPath.startsWith("/")) logoPath = "/" + logoPath;
            try (var imageStream = getClass().getResourceAsStream(logoPath)) {
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

    private void loadHistoryList() {
        historyBox.getChildren().clear();
        List<UserHistory> history = historyService.getUserHistory(userId);

        for (UserHistory entry : history) {
            RestaurantDAO.getRestaurantById(entry.restaurantId())
                    .ifPresent(restaurant -> {
                        Button historyBtn = new Button(restaurant.name());
                        historyBtn.setStyle("-fx-font-size: 16; -fx-background-color: transparent; -fx-text-fill: #222;");
                        historyBtn.setOnAction(_ -> Navigation.load(
                                "/com/idktogo/idk_to_go/restaurant.fxml",
                                controller -> ((RestaurantController) controller).setRestaurantId(restaurant.id())
                        ));
                        historyBox.getChildren().add(historyBtn);
                    });
        }
    }

    @FXML
    private void openClosest() {
        Navigation.load("/com/idktogo/idk_to_go/closest.fxml");
    }

    @FXML
    private void openQuiz() {
        Navigation.load("/com/idktogo/idk_to_go/quiz.fxml");
    }

    @FXML
    private void openTrending() {
        Navigation.load("/com/idktogo/idk_to_go/trending.fxml");
    }

    @FXML
    private void openOptions() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }

    @FXML
    private void openRestaurantScene() {
        RestaurantDAO.getTopRestaurantsByWeeklyLikes(1).stream()
                .findFirst()
                .ifPresentOrElse(
                        restaurant -> Navigation.load(
                                "/com/idktogo/idk_to_go/restaurant.fxml",
                                controller -> ((RestaurantController) controller).setRestaurantId(restaurant.id())
                        ),
                        () -> System.err.println("No hot restaurant found"));
    }

    private void openUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            System.err.println("Could not open URL: " + e.getMessage());
        }
    }
}

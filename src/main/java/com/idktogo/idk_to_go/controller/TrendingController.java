package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.Restaurant;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TrendingController {

    @FXML private VBox trendingList;
    @FXML private Button weeklyButton;
    @FXML private Button allTimeButton;

    @FXML
    private void initialize() {
        highlightTab(weeklyButton);
        loadWeeklyTrending();
    }

    // Show weekly trending restaurants
    @FXML
    private void showWeekly() {
        highlightTab(weeklyButton);
        loadWeeklyTrending();
    }

    private void loadWeeklyTrending() {
        trendingList.getChildren().clear();

        RestaurantDAO.topByWeeklyLikes(10)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    trendingList.getChildren().clear();

                    if (restaurants.isEmpty()) {
                        trendingList.getChildren().add(new Label("No trending restaurants this week."));
                        return;
                    }

                    for (Restaurant r : restaurants) {
                        trendingList.getChildren().add(createRestaurantRow(r));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> trendingList.getChildren().add(
                            new Label("Error loading weekly trending: " + ex.getMessage())));
                    return null;
                });
    }

    // Show all-time trending restaurants
    @FXML
    private void showAllTime() {
        highlightTab(allTimeButton);
        loadAllTimeTrending();
    }

    private void loadAllTimeTrending() {
        trendingList.getChildren().clear();

        RestaurantDAO.topByNetScore()
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    trendingList.getChildren().clear();

                    if (restaurants.isEmpty()) {
                        trendingList.getChildren().add(new Label("No all-time trending restaurants found."));
                        return;
                    }

                    for (Restaurant r : restaurants) {
                        trendingList.getChildren().add(createRestaurantRow(r));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> trendingList.getChildren().add(
                            new Label("Error loading all-time trending: " + ex.getMessage())));
                    return null;
                });
    }

    // Create a display row for a restaurant
    private HBox createRestaurantRow(Restaurant restaurant) {
        HBox row = new HBox(10);
        row.setStyle("""
            -fx-padding: 8;
            -fx-cursor: hand;
            -fx-alignment: CENTER_LEFT;
        """.trim());

        Label nameLabel = new Label(restaurant.name());
        nameLabel.setStyle("-fx-font-size: 16;");

        Label scoreLabel = new Label("Score: " + restaurant.netScore());
        scoreLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        row.getChildren().addAll(nameLabel, scoreLabel);
        row.setOnMouseClicked(event -> openRestaurantScene(restaurant.id()));
        return row;
    }

    // Open the restaurant details scene
    private void openRestaurantScene(int restaurantId) {
        Navigation.load("/com/idktogo/idk_to_go/restaurant.fxml", controller -> {
            if (controller instanceof RestaurantController rc) {
                rc.setRestaurantId(restaurantId);
            }
        });
    }

    // Highlight the active tab button
    private void highlightTab(Button activeButton) {
        String inactiveStyle = "-fx-background-color: #dfe6e9; -fx-padding: 5 15;";
        String activeStyle   = "-fx-background-color: #00BFFF; -fx-padding: 5 15; -fx-text-fill: white;";

        weeklyButton.setStyle(inactiveStyle);
        allTimeButton.setStyle(inactiveStyle);
        activeButton.setStyle(activeStyle);
    }

    // Navigate back to the main scene
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }
}

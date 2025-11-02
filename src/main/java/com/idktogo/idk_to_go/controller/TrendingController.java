package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.Restaurant;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class TrendingController {

    @FXML private VBox trendingList;
    @FXML private Button weeklyButton;
    @FXML private Button allTimeButton;

    @FXML
    private void initialize() {
        highlightTab(weeklyButton);
        loadWeeklyTrending();
    }

    // === Weekly Tab ===
    @FXML
    private void showWeekly() {
        highlightTab(weeklyButton);
        loadWeeklyTrending();
    }

    private void loadWeeklyTrending() {
        trendingList.getChildren().clear();
        List<Restaurant> trending = RestaurantDAO.getTopRestaurantsByWeeklyLikes();
        trending.forEach(r -> trendingList.getChildren().add(createRestaurantRow(r)));
    }

    // === All-Time Tab ===
    @FXML
    private void showAllTime() {
        highlightTab(allTimeButton);
        loadAllTimeTrending();
    }

    private void loadAllTimeTrending() {
        trendingList.getChildren().clear();
        List<Restaurant> trending = RestaurantDAO.getTopRestaurantsByNetScore();
        trending.forEach(r -> trendingList.getChildren().add(createRestaurantRow(r)));
    }

    // === Shared Helpers ===
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

    private void openRestaurantScene(int restaurantId) {
        Navigation.load("/com/idktogo/idk_to_go/restaurant.fxml", controller -> {
            if (controller instanceof RestaurantController rc) {
                rc.setRestaurantId(restaurantId);
            }
        });
    }

    private void highlightTab(Button activeButton) {
        String inactiveStyle = "-fx-background-color: #dfe6e9; -fx-padding: 5 15;";
        String activeStyle   = "-fx-background-color: #00BFFF; -fx-padding: 5 15; -fx-text-fill: white;";

        weeklyButton.setStyle(inactiveStyle);
        allTimeButton.setStyle(inactiveStyle);
        activeButton.setStyle(activeStyle);
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }
}

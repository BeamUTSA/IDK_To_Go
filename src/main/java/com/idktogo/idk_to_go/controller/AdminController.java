package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.HistoryDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.Restaurant;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminController {

    @FXML private VBox restaurantList;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField locationField;
    @FXML private TextField logoField;

    @FXML
    private void initialize() {
        loadRestaurants();
    }

    private void loadRestaurants() {
        restaurantList.getChildren().clear();

        List<Restaurant> restaurants = RestaurantDAO.getAllRestaurantsSortedByName();
        for (Restaurant r : restaurants) {
            HBox row = new HBox(10);
            row.setStyle("-fx-padding: 8; -fx-alignment: CENTER_LEFT;");

            Label nameLabel = new Label(r.name());
            nameLabel.setStyle("-fx-font-size: 16;");

            Button menuBtn = new Button("Menu");
            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            menuBtn.setOnAction(e -> manageMenu(r.id()));
            editBtn.setOnAction(e -> editRestaurant(r.id()));
            deleteBtn.setOnAction(e -> deleteRestaurant(r.id()));

            row.getChildren().addAll(nameLabel, menuBtn, editBtn, deleteBtn);
            restaurantList.getChildren().add(row);
        }
    }

    @FXML
    private void addRestaurant() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String location = locationField.getText().trim();
        String logo = logoField.getText().trim();

        if (name.isBlank() || logo.isBlank()) {
            System.out.println("Name and logo are required.");
            return;
        }

        Restaurant newRestaurant = new Restaurant(
                0, name, category, location,
                0, 0, 0, 0, logo
        );

        if (RestaurantDAO.addRestaurant(newRestaurant)) {
            System.out.println("Restaurant added successfully.");
            clearFields();
            loadRestaurants();
        } else {
            System.out.println("Failed to add restaurant.");
        }
    }

    private void clearFields() {
        nameField.clear();
        categoryField.clear();
        locationField.clear();
        logoField.clear();
    }

    private void editRestaurant(int restaurantId) {
        System.out.println("Edit restaurant " + restaurantId + " ... (not yet implemented)");
        // TODO: Build an Edit Restaurant screen and navigate there
    }

    private void deleteRestaurant(int restaurantId) {
        if (RestaurantDAO.deleteRestaurant(restaurantId)) {
            System.out.println("Restaurant deleted");
            loadRestaurants();
        } else {
            System.out.println("Delete failed");
        }
    }

    private void manageMenu(int restaurantId) {
        Navigation.load("/com/idktogo/idk_to_go/admin_menu.fxml", controller -> {
            if (controller instanceof MenuController mc) {
                mc.setRestaurantId(restaurantId);
            }
        });
    }

    @FXML
    private void resetWeeklyStats() {
        RestaurantDAO.resetWeeklyLikes();
        System.out.println("Weekly likes reset");
        loadRestaurants();
    }

    @FXML
    private void clearAllHistory() {
        HistoryDAO.deleteHistoryForAllUsers();
        System.out.println("All user history cleared");
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }
}

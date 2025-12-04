package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.HistoryDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.Restaurant;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    // Load all restaurants into the list
    private void loadRestaurants() {
        restaurantList.getChildren().clear();

        CompletableFuture<List<Restaurant>> future = RestaurantDAO.listAll();

        future.thenAccept(restaurants -> Platform.runLater(() -> {
            restaurantList.getChildren().clear();

            if (restaurants.isEmpty()) {
                restaurantList.getChildren().add(new Label("No restaurants found."));
                return;
            }

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

        })).exceptionally(e -> {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Error loading restaurants", e.getMessage()));
            return null;
        });
    }

    // Add a new restaurant
    @FXML
    private void addRestaurant() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String location = locationField.getText().trim();
        String logo = logoField.getText().trim();

        if (name.isBlank() || logo.isBlank()) {
            showAlert("Invalid Input", "Name and logo are required.");
            return;
        }

        Restaurant newRestaurant = new Restaurant(
                0,
                name,
                category,
                location,
                0, 0, 0, 0,
                logo
        );

        RestaurantDAO.create(newRestaurant)
                .thenRun(() -> Platform.runLater(() -> {
                    showAlert("Success", "Restaurant added successfully.");
                    clearFields();
                    loadRestaurants();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error adding restaurant", e.getMessage()));
                    return null;
                });
    }

    // Clear input fields
    private void clearFields() {
        nameField.clear();
        categoryField.clear();
        locationField.clear();
        logoField.clear();
    }

    // Edit an existing restaurant
    private void editRestaurant(int restaurantId) {
        showAlert("Info", "Edit restaurant " + restaurantId + " (not implemented yet).");
    }

    // Delete a restaurant
    private void deleteRestaurant(int restaurantId) {
        RestaurantDAO.delete(restaurantId)
                .thenRun(() -> Platform.runLater(() -> {
                    showAlert("Deleted", "Restaurant deleted successfully.");
                    loadRestaurants();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error deleting restaurant", e.getMessage()));
                    return null;
                });
    }

    // Manage menu items for a restaurant
    private void manageMenu(int restaurantId) {
        Navigation.load("/com/idktogo/idk_to_go/admin_menu.fxml", controller -> {
            if (controller instanceof MenuController mc) {
                mc.setRestaurantId(restaurantId);
            }
        });
    }

    // Reset weekly likes for all restaurants
    @FXML
    private void resetWeeklyStats() {
        RestaurantDAO.resetWeeklyLikes()
                .thenRun(() -> Platform.runLater(() -> {
                    showAlert("Success", "Weekly likes reset.");
                    loadRestaurants();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error resetting stats", e.getMessage()));
                    return null;
                });
    }

    // Clear all user history
    @FXML
    private void clearAllHistory() {
        HistoryDAO.deleteAllForAllUsers()
                .thenRun(() -> Platform.runLater(() -> showAlert("Cleared", "All user history cleared.")))
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error clearing history", e.getMessage()));
                    return null;
                });
    }

    // Navigate back to the previous scene
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }

    // Show an alert dialog
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.MenuItemDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.MenuItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MenuController {

    @FXML private Label restaurantNameLabel;
    @FXML private VBox menuItemsBox;
    @FXML private TextField itemNameField;
    @FXML private TextField itemPriceField;

    private int restaurantId;

    // Called when restaurant is selected
    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
        loadMenuItems();
    }

    // === LOAD RESTAURANT INFO + MENU ===
    private void loadMenuItems() {
        menuItemsBox.getChildren().clear();

        // Load restaurant name
        RestaurantDAO.findById(restaurantId)
                .thenAccept(restaurantOpt -> restaurantOpt.ifPresent(r ->
                        Platform.runLater(() ->
                                restaurantNameLabel.setText(r.name() + " Menu")
                        )))
                .exceptionally(ex -> {
                    System.err.println("Failed to load restaurant: " + ex.getMessage());
                    return null;
                });

        // Load menu items
        MenuItemDAO.listByRestaurant(restaurantId)
                .thenAccept(items -> Platform.runLater(() -> {
                    menuItemsBox.getChildren().clear();

                    if (items.isEmpty()) {
                        menuItemsBox.getChildren().add(new Label("No menu items available"));
                        return;
                    }

                    for (MenuItem item : items) {
                        HBox row = new HBox(10);
                        Label itemNameLabel = new Label(item.itemName());
                        Label itemPriceLabel = new Label(String.format("$%.2f", item.price()));
                        Button deleteButton = new Button("Delete");

                        HBox.setHgrow(itemNameLabel, Priority.ALWAYS);
                        itemNameLabel.setMaxWidth(Double.MAX_VALUE);

                        deleteButton.setOnAction(e -> deleteMenuItem(item.id()));

                        row.getChildren().addAll(itemNameLabel, itemPriceLabel, deleteButton);
                        menuItemsBox.getChildren().add(row);
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("Failed to load menu items: " + ex.getMessage());
                    return null;
                });
    }

    // === ADD MENU ITEM ===
    @FXML
    private void addMenuItem() {
        String name = itemNameField.getText().trim();
        String priceText = itemPriceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            showAlert("Error", "Please enter both a name and price.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);

            MenuItem newItem = new MenuItem(0, restaurantId, name, price);

            MenuItemDAO.create(newItem)
                    .thenRun(() -> Platform.runLater(() -> {
                        itemNameField.clear();
                        itemPriceField.clear();
                        loadMenuItems();
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() ->
                                showAlert("Error", "Failed to add item: " + ex.getMessage()));
                        return null;
                    });

        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid price format. Please enter a valid number.");
        }
    }

    // === DELETE MENU ITEM ===
    private void deleteMenuItem(int menuItemId) {
        MenuItemDAO.delete(menuItemId)
                .thenRun(() -> Platform.runLater(this::loadMenuItems))
                .exceptionally(ex -> {
                    System.err.println("Failed to delete menu item: " + ex.getMessage());
                    return null;
                });
    }

    // === UTILITY ===
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/admin.fxml");
    }
}

package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.MenuItemDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.MenuItem;
import com.idktogo.idk_to_go.model.Restaurant;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class MenuController {

    @FXML private Label restaurantNameLabel;
    @FXML private VBox menuItemsBox;
    @FXML private TextField itemNameField;
    @FXML private TextField itemPriceField;

    private int restaurantId;

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
        loadMenuItems();
    }

    private void loadMenuItems() {
        menuItemsBox.getChildren().clear();

        // Load restaurant name
        Optional<Restaurant> restaurant = RestaurantDAO.getRestaurantById(restaurantId);
        restaurant.ifPresent(r -> restaurantNameLabel.setText(r.name() + " Menu"));

        // Load menu items
        List<MenuItem> items = MenuItemDAO.getMenuItemsByRestaurant(restaurantId);
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
    }

    @FXML
    private void addMenuItem() {
        String name = itemNameField.getText().trim();
        String priceText = itemPriceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            System.out.println("Please enter both name and price.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            MenuItem newItem = new MenuItem(0, restaurantId, name, price);
            boolean success = MenuItemDAO.addMenuItem(newItem);

            if (success) {
                loadMenuItems();
                itemNameField.clear();
                itemPriceField.clear();
                System.out.println("Menu item added.");
            } else {
                System.out.println("Failed to add menu item.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
        }
    }

    private void deleteMenuItem(int itemId) {
        MenuItemDAO.deleteMenuItem(itemId);
        loadMenuItems();
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/admin.fxml");
    }
}

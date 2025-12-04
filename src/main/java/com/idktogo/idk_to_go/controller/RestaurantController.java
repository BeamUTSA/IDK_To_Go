package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.SessionManager;
import com.idktogo.idk_to_go.dao.MenuItemDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.model.MenuItem;
import com.idktogo.idk_to_go.model.Restaurant;
import com.idktogo.idk_to_go.service.RestaurantService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;

public class RestaurantController {

    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label locationLabel;
    @FXML private ImageView logoImage;
    @FXML private VBox menuList;

    private int restaurantId;
    private String mapsUrl;

    // Sets the restaurant ID and loads its details
    public void setRestaurantId(int id) {
        this.restaurantId = id;
        loadRestaurantDetails();
    }

    // Loads restaurant details from the database
    private void loadRestaurantDetails() {
        RestaurantDAO.findById(restaurantId)
                .thenAccept(optionalRestaurant -> Platform.runLater(() -> {
                    if (optionalRestaurant.isEmpty()) {
                        nameLabel.setText("Restaurant Not Found");
                        return;
                    }

                    Restaurant r = optionalRestaurant.get();
                    nameLabel.setText(r.name());
                    categoryLabel.setText("Category: " + r.category());
                    mapsUrl = r.location();
                    locationLabel.setText("Open in Maps");

                    // Load restaurant logo
                    if (r.logo() != null && !r.logo().isBlank()) {
                        try {
                            String logoPath = r.logo();
                            if (!logoPath.startsWith("/")) logoPath = "/" + logoPath;
                            try (InputStream stream = getClass().getResourceAsStream(logoPath)) {
                                if (stream != null) {
                                    logoImage.setImage(new Image(stream));
                                } else {
                                    System.err.println("Logo not found: " + logoPath);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading logo: " + e.getMessage());
                        }
                    }

                    loadMenuItems();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            nameLabel.setText("Error loading restaurant: " + ex.getMessage()));
                    return null;
                });
    }

    // Loads menu items for the current restaurant
    private void loadMenuItems() {
        menuList.getChildren().clear();

        MenuItemDAO.listByRestaurant(restaurantId)
                .thenAccept(items -> Platform.runLater(() -> {
                    menuList.getChildren().clear();

                    if (items.isEmpty()) {
                        menuList.getChildren().add(new Label("No menu items available"));
                        return;
                    }

                    for (MenuItem item : items) {
                        HBox row = new HBox(10);
                        Label itemName = new Label(item.itemName());
                        Label itemPrice = new Label(String.format("$%.2f", item.price()));

                        itemName.setStyle("-fx-font-size: 14px;");
                        itemPrice.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                        HBox.setHgrow(itemName, Priority.ALWAYS);
                        row.getChildren().addAll(itemName, itemPrice);
                        menuList.getChildren().add(row);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> menuList.getChildren().add(
                            new Label("Error loading menu items: " + ex.getMessage())));
                    return null;
                });
    }

    // Handles liking a restaurant
    @FXML
    private void likeRestaurant() {
        Integer userId = SessionManager.getUserId();
        if (userId == null) {
            System.err.println("User not logged in — like ignored.");
            return;
        }

        RestaurantService.handleLike(userId, restaurantId)
                .thenRun(() -> Platform.runLater(() ->
                        System.out.println("Restaurant liked successfully.")))
                .exceptionally(ex -> {
                    System.err.println("Error liking restaurant: " + ex.getMessage());
                    return null;
                });
    }

    // Handles disliking a restaurant
    @FXML
    private void dislikeRestaurant() {
        Integer userId = SessionManager.getUserId();
        if (userId == null) {
            System.err.println("User not logged in — dislike ignored.");
            return;
        }

        RestaurantService.handleDislike(userId, restaurantId)
                .thenRun(() -> Platform.runLater(() ->
                        System.out.println("Restaurant disliked successfully.")))
                .exceptionally(ex -> {
                    System.err.println("Error disliking restaurant: " + ex.getMessage());
                    return null;
                });
    }

    // Opens the restaurant's location in a map application
    @FXML
    private void openInMaps() {
        if (mapsUrl != null && !mapsUrl.isBlank()) {
            try {
                Desktop.getDesktop().browse(new URI(mapsUrl));
            } catch (Exception e) {
                System.err.println("Could not open maps URL: " + mapsUrl);
            }
        }
    }

    // Navigates back to the main scene
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }
}

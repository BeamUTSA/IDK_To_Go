package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.MenuItemDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.model.MenuItem;
import com.idktogo.idk_to_go.model.Restaurant;
import com.idktogo.idk_to_go.service.UserHistoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class RestaurantController {

    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label locationLabel;
    @FXML private ImageView logoImage;
    @FXML private VBox menuList;

    private int restaurantId;
    private String mapsUrl;

    private final UserHistoryService historyService = UserHistoryService.getInstance();
    private final int userId = UserDAO.getLoggedInUserId();

    public void setRestaurantId(int id) {
        this.restaurantId = id;
        loadRestaurantDetails();
    }

    private void loadRestaurantDetails() {
        Optional<Restaurant> optionalRestaurant = RestaurantDAO.getRestaurantById(restaurantId);

        if (optionalRestaurant.isEmpty()) {
            nameLabel.setText("Restaurant Not Found");
            return;
        }

        Restaurant r = optionalRestaurant.get();
        nameLabel.setText(r.name());
        categoryLabel.setText("Category: " + r.category());
        mapsUrl = r.location();
        locationLabel.setText("Open in Maps");

        if (r.logo() != null && !r.logo().isBlank()) {
            logoImage.setImage(new Image(r.logo(), true));
        }

        loadMenuItems();
    }

    private void loadMenuItems() {
        menuList.getChildren().clear();
        List<MenuItem> items = MenuItemDAO.getMenuItemsByRestaurant(restaurantId);

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
    }

    @FXML
    private void likeRestaurant() {
        historyService.recordAction(userId, restaurantId, true);
    }

    @FXML
    private void dislikeRestaurant() {
        historyService.recordAction(userId, restaurantId, false);
    }

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

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }
}

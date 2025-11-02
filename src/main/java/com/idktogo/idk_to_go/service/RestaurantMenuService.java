package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.dao.MenuItemDAO;
import com.idktogo.idk_to_go.model.MenuItem;

import java.util.List;
import java.util.Optional;

public final class RestaurantMenuService {

    private RestaurantMenuService() {}

    /**
     * Adds a new menu item for a given restaurant.
     * Includes basic validation rules.
     */
    public static boolean addMenuItem(int restaurantId, String itemName, double price) {
        if (itemName == null || itemName.trim().isEmpty()) {
            System.err.println("Item name cannot be blank");
            return false;
        }
        if (price <= 0) {
            System.err.println("Price must be positive");
            return false;
        }

        MenuItem item = new MenuItem(0, restaurantId, itemName.trim(), price);
        return MenuItemDAO.addMenuItem(item);
    }

    /**
     * Updates a menu item’s name and price.
     * Returns true if update is successful.
     */
    public static boolean updateMenuItem(int id, String newName, double newPrice) {
        Optional<MenuItem> existing = MenuItemDAO.getMenuItemById(id);

        if (existing.isEmpty()) {
            System.err.println("Cannot update: Menu item not found with ID " + id);
            return false;
        }

        if (newName == null || newName.trim().isEmpty()) {
            System.err.println("New name cannot be blank");
            return false;
        }

        if (newPrice <= 0) {
            System.err.println("New price must be a positive number");
            return false;
        }

        // Create an updated record
        MenuItem updated = new MenuItem(
                id,
                existing.get().restaurantId(), // preserve restaurant association
                newName.trim(),
                newPrice
        );

        // DAO does not have update method yet - build one (optional)
        // For now, delete + re-add as temporary workaround:
        MenuItemDAO.deleteMenuItem(id);
        return MenuItemDAO.addMenuItem(updated);
    }

    /**
     * Deletes a menu item by ID.
     */
    public static boolean deleteMenuItem(int id) {
        return MenuItemDAO.deleteMenuItem(id);
    }

    /**
     * Lists all menu items for a restaurant.
     */
    public static List<MenuItem> getMenuForRestaurant(int restaurantId) {
        return MenuItemDAO.getMenuItemsByRestaurant(restaurantId);
    }

    /**
     * Performs a case-insensitive search for menu items by name within a restaurant.
     */
    public static List<MenuItem> searchMenu(String partialName, int restaurantId) {
        if (partialName == null || partialName.trim().isEmpty()) {
            // Returns full menu if no search input
            return getMenuForRestaurant(restaurantId);
        }
        return MenuItemDAO.findMenuItemsWithNameLike(partialName, restaurantId);
    }

    /**
     * Retrieves a specific menu item by ID.
     */
    public static Optional<MenuItem> getMenuItemById(int id) {
        return MenuItemDAO.getMenuItemById(id);
    }
}

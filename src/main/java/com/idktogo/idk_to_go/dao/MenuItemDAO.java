package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.core.DatabaseConnector;
import com.idktogo.idk_to_go.model.MenuItem;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class MenuItemDAO {

    private MenuItemDAO() {}

    // Create a new menu item
    public static CompletableFuture<Void> create(MenuItem item) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO menu_items (restaurant_id, item_name, price) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, item.restaurantId());
                ps.setString(2, item.itemName());
                ps.setDouble(3, item.price());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to add menu item: " + e.getMessage(), e);
            }
        });
    }

    // List all menu items for a given restaurant
    public static CompletableFuture<List<MenuItem>> listByRestaurant(int restaurantId) {
        return CompletableFuture.supplyAsync(() -> {
            List<MenuItem> list = new ArrayList<>();
            String sql = "SELECT * FROM menu_items WHERE restaurant_id = ? ORDER BY item_name ASC";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, restaurantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to list menu items: " + e.getMessage(), e);
            }
            return list;
        });
    }

    // Update an existing menu item
    public static CompletableFuture<Boolean> update(MenuItem item) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE menu_items SET item_name = ?, price = ? WHERE id = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.itemName());
                ps.setDouble(2, item.price());
                ps.setInt(3, item.id());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update menu item: " + e.getMessage(), e);
            }
        });
    }

    // Delete a menu item by ID
    public static CompletableFuture<Boolean> delete(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM menu_items WHERE id = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete menu item: " + e.getMessage(), e);
            }
        });
    }

    // Map a ResultSet row to a MenuItem object
    private static MenuItem mapRow(ResultSet rs) throws SQLException {
        return new MenuItem(
                rs.getInt("id"),
                rs.getInt("restaurant_id"),
                rs.getString("item_name"),
                rs.getDouble("price")
        );
    }
}

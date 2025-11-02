package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.data.SQLiteManager;
import com.idktogo.idk_to_go.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MenuItemDAO {

    private MenuItemDAO() {}

    // === CRUD OPERATIONS ===

    /** Inserts a new menu item into the database */
    public static boolean addMenuItem(MenuItem item) {
        final String sql = """
            INSERT INTO menu_items (restaurant_id, item_name, price)
            VALUES (?, ?, ?)
        """;
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.restaurantId());
            ps.setString(2, item.itemName());
            ps.setDouble(3, item.price());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("MenuItemDAO.addMenuItem failed: " + e.getMessage());
            return false;
        }
    }

    /** Retrieves all menu items for a given restaurant ID */
    public static List<MenuItem> getMenuItemsByRestaurant(int restaurantId) {
        final String sql = "SELECT * FROM menu_items WHERE restaurant_id = ?";
        List<MenuItem> items = new ArrayList<>();

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("MenuItemDAO.getMenuItemsByRestaurant failed: " + e.getMessage());
        }

        return items;
    }

    /** Retrieves a menu item by ID */
    public static Optional<MenuItem> getMenuItemById(int id) {
        final String sql = "SELECT * FROM menu_items WHERE id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToMenuItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("MenuItemDAO.getMenuItemById failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Deletes a menu item by ID */
    public static boolean deleteMenuItem(int id) {
        final String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("MenuItemDAO.deleteMenuItem failed: " + e.getMessage());
            return false;
        }
    }

    /** Deletes all menu items for a restaurant */
    public static boolean deleteMenuItemsByRestaurant(int restaurantId) {
        final String sql = "DELETE FROM menu_items WHERE restaurant_id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("MenuItemDAO.deleteMenuItemsByRestaurant failed: " + e.getMessage());
            return false;
        }
    }

    /** Search by partial menu item name (case insensitive) */
    public static List<MenuItem> findMenuItemsWithNameLike(String partialName, int restaurantId) {
        final List<MenuItem> items = new ArrayList<>();
        final String sql = """
            SELECT * FROM menu_items 
            WHERE restaurant_id = ? AND LOWER(item_name) LIKE LOWER(?)
            ORDER BY LOWER(item_name)
        """;

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);
            ps.setString(2, "%" + partialName + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(mapRowToMenuItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("MenuItemDAO.findMenuItemsWithNameLike failed: " + e.getMessage());
        }

        return items;
    }

    // === HELPER MAPPER ===

    private static MenuItem mapRowToMenuItem(ResultSet rs) throws SQLException {
        return new MenuItem(
                rs.getInt("id"),
                rs.getInt("restaurant_id"),
                rs.getString("item_name"),
                rs.getDouble("price")
        );
    }
}

package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.data.SQLiteManager;
import com.idktogo.idk_to_go.model.Restaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RestaurantDAO {

    private RestaurantDAO() {}

    // === CRUD (unchanged parts omitted for brevity)… ===

    /** Helper method to map result row to Restaurant record */
    private static Restaurant mapRowToRestaurant(ResultSet rs) throws SQLException {
        return new Restaurant(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("location"),
                rs.getInt("likes"),
                rs.getInt("dislikes"),
                rs.getInt("net_score"),
                rs.getInt("weekly_likes"),
                rs.getString("logo")
        );
    }

    public static boolean addRestaurant(Restaurant r) {
        final String sql = """
        INSERT INTO restaurants (name, category, location, likes, dislikes, net_score, weekly_likes, logo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.name());
            ps.setString(2, r.category());
            ps.setString(3, r.location());
            ps.setInt(4, r.likes());
            ps.setInt(5, r.dislikes());
            ps.setInt(6, r.netScore());
            ps.setInt(7, r.weeklyLikes());
            ps.setString(8, r.logo());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("RestaurantDAO.addRestaurant failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteRestaurant(int restaurantId) {
        final String sql = "DELETE FROM restaurants WHERE id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RestaurantDAO.deleteRestaurant failed: " + e.getMessage());
            return false;
        }
    }

    /** Retrieves all restaurants sorted by name (case-insensitive) */
    public static List<Restaurant> getAllRestaurantsSortedByName() {
        final List<Restaurant> restaurants = new ArrayList<>();
        final String sql = "SELECT * FROM restaurants ORDER BY LOWER(name) ASC";

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                restaurants.add(mapRowToRestaurant(rs));
            }

        } catch (SQLException e) {
            System.err.println("RestaurantDAO.getAllRestaurantsSortedByName failed: " + e.getMessage());
        }

        return restaurants;
    }

    /** Retrieves a restaurant by ID */
    public static Optional<Restaurant> getRestaurantById(int id) {
        final String sql = "SELECT * FROM restaurants WHERE id = ?";

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToRestaurant(rs));
            }

        } catch (SQLException e) {
            System.err.println("RestaurantDAO.getRestaurantById failed: " + e.getMessage());
        }

        return Optional.empty();
    }

    // === SCORING OPERATIONS ===

    public static void incrementLikes(int restaurantId) {
        executeScoreUpdate("UPDATE restaurants SET likes = likes + 1 WHERE id = ?", restaurantId);
    }

    public static void decrementLikes(int restaurantId) {
        // safe: do not drop below zero
        executeScoreUpdate("""
            UPDATE restaurants 
            SET likes = CASE WHEN likes > 0 THEN likes - 1 ELSE 0 END 
            WHERE id = ?
        """, restaurantId);
    }

    public static void incrementDislikes(int restaurantId) {
        executeScoreUpdate("UPDATE restaurants SET dislikes = dislikes + 1 WHERE id = ?", restaurantId);
    }

    public static void decrementDislikes(int restaurantId) {
        // safe: do not drop below zero
        executeScoreUpdate("""
            UPDATE restaurants 
            SET dislikes = CASE WHEN dislikes > 0 THEN dislikes - 1 ELSE 0 END 
            WHERE id = ?
        """, restaurantId);
    }

    /**
     * Adjust net_score and weekly_likes together by a signed delta.
     * Example: delta=+2 when switching from dislike -> like.
     */
    public static void adjustNetAndWeekly(int restaurantId, int delta) {
        final String sql = "UPDATE restaurants SET net_score = net_score + ?, weekly_likes = weekly_likes + ? WHERE id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, delta);
            ps.setInt(3, restaurantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("RestaurantDAO.adjustNetAndWeekly failed: " + e.getMessage());
        }
    }

    /** Helper method to run simple update queries */
    private static void executeScoreUpdate(String sql, int restaurantId) {
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("RestaurantDAO.executeScoreUpdate failed: " + e.getMessage());
        }
    }

    /** Retrieves all restaurants sorted by all-time net score (descending) */
    public static List<Restaurant> getTopRestaurantsByNetScore() {
        final List<Restaurant> restaurants = new ArrayList<>();
        final String sql = "SELECT * FROM restaurants ORDER BY net_score DESC";

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                restaurants.add(mapRowToRestaurant(rs));
            }

        } catch (SQLException e) {
            System.err.println("RestaurantDAO.getTopRestaurantsByNetScore failed: " + e.getMessage());
        }

        return restaurants;
    }

    /** Returns the Restaurant with the highest weekly_likes */
    public static List<Restaurant> getTopRestaurantsByWeeklyLikes(int limit) {
        final List<Restaurant> restaurants = new ArrayList<>();
        final String sql = "SELECT * FROM restaurants ORDER BY weekly_likes DESC LIMIT ?";

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                restaurants.add(mapRowToRestaurant(rs));
            }

        } catch (SQLException e) {
            System.err.println("RestaurantDAO.getTopRestaurantsByWeeklyLikes failed: " + e.getMessage());
        }

        return restaurants;
    }

    /** Retrieves all restaurants sorted by weekly likes (descending) */
    public static List<Restaurant> getTopRestaurantsByWeeklyLikes() {
        final List<Restaurant> restaurants = new ArrayList<>();
        final String sql = "SELECT * FROM restaurants ORDER BY weekly_likes DESC";

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                restaurants.add(mapRowToRestaurant(rs));
            }

        } catch (SQLException e) {
            System.err.println("RestaurantDAO.getTopRestaurantsByWeeklyLikes failed: " + e.getMessage());
        }

        return restaurants;
    }

    /** Reset weekly likes for all restaurants (to be run periodically) */
    public static void resetWeeklyLikes() {
        final String sql = "UPDATE restaurants SET weekly_likes = 0";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("RestaurantDAO.resetWeeklyLikes failed: " + e.getMessage());
        }
    }

    // Keep your existing list/get methods as-is.
    // Note: SQLite generally allows `LIMIT ?`. If your driver balks, inline the int into SQL safely.
}

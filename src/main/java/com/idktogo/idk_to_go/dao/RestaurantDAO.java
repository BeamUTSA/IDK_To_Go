package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.core.DatabaseConnector;
import com.idktogo.idk_to_go.model.Restaurant;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class RestaurantDAO {

    private RestaurantDAO() {}

    // Create a new restaurant
    public static CompletableFuture<Void> create(Restaurant r) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO restaurants (name, category, location, likes, dislikes, netScore, weeklyLikes, logo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, r.name());
                ps.setString(2, r.category());
                ps.setString(3, r.location());
                ps.setInt(4, r.likes());
                ps.setInt(5, r.dislikes());
                ps.setInt(6, r.netScore());
                ps.setInt(7, r.weeklyLikes());
                ps.setString(8, r.logo());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create restaurant: " + e.getMessage(), e);
            }
        });
    }

    // Find a restaurant by ID
    public static CompletableFuture<Optional<Restaurant>> findById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM restaurants WHERE id = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return Optional.of(mapRow(rs));
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find restaurant: " + e.getMessage(), e);
            }
            return Optional.empty();
        });
    }

    // List all restaurants
    public static CompletableFuture<List<Restaurant>> listAll() {
        return queryList("SELECT * FROM restaurants ORDER BY name ASC");
    }

    // Get top restaurants by weekly likes
    public static CompletableFuture<List<Restaurant>> topByWeeklyLikes(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = """
        SELECT id, name, category, location, likes, dislikes, netScore, weeklyLikes, logo
        FROM restaurants
        ORDER BY weeklyLikes DESC
        LIMIT """ + " " + safeLimit;

        return CompletableFuture.supplyAsync(() -> {
            List<Restaurant> list = new ArrayList<>();
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            } catch (SQLException e) {
                throw new RuntimeException("Query failed in topByWeeklyLikes: " + e.getMessage(), e);
            }
            return list;
        });
    }

    // Get top restaurants by net score
    public static CompletableFuture<List<Restaurant>> topByNetScore() {
        String sql = """
        SELECT id, name, category, location, likes, dislikes, netScore, weeklyLikes, logo
        FROM restaurants
        ORDER BY netScore DESC
        LIMIT 50
    """;

        return CompletableFuture.supplyAsync(() -> {
            List<Restaurant> list = new ArrayList<>();
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            } catch (SQLException e) {
                throw new RuntimeException("Query failed in topByNetScore: " + e.getMessage(), e);
            }
            return list;
        });
    }

    // Update an existing restaurant
    public static CompletableFuture<Boolean> update(Restaurant r) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                UPDATE restaurants
                SET name=?, category=?, location=?, logo=? WHERE id=?
            """;
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, r.name());
                ps.setString(2, r.category());
                ps.setString(3, r.location());
                ps.setString(4, r.logo());
                ps.setInt(5, r.id());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update restaurant: " + e.getMessage(), e);
            }
        });
    }

    // Delete a restaurant by ID
    public static CompletableFuture<Void> delete(int id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM restaurants WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete restaurant: " + e.getMessage(), e);
            }
        });
    }

    // Increment restaurant likes
    public static void incrementLikes(int id) { adjustField(id, "likes", 1); }
    // Decrement restaurant likes
    public static void decrementLikes(int id) { adjustField(id, "likes", -1); }
    // Increment restaurant dislikes
    public static void incrementDislikes(int id) { adjustField(id, "dislikes", 1); }
    // Decrement restaurant dislikes
    public static void decrementDislikes(int id) { adjustField(id, "dislikes", -1); }

    // Adjust net score and weekly likes
    public static void adjustNetAndWeekly(int id, int delta) {
        executeRaw("UPDATE restaurants SET netScore = netScore + ?, weeklyLikes = weeklyLikes + ? WHERE id = ?", ps -> {
            ps.setInt(1, delta);
            ps.setInt(2, delta);
            ps.setInt(3, id);
        });
    }

    // Reset all weekly likes to zero
    public static CompletableFuture<Void> resetWeeklyLikes() {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE restaurants SET weeklyLikes = 0";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to reset weekly likes: " + e.getMessage(), e);
            }
        });
    }

    // Adjust a specific field (likes/dislikes)
    private static void adjustField(int id, String field, int delta) {
        executeRaw("UPDATE restaurants SET " + field + " = GREATEST(" + field + " + ?, 0) WHERE id = ?", ps -> {
            ps.setInt(1, delta);
            ps.setInt(2, id);
        });
    }

    // Map a ResultSet row to a Restaurant object
    static Restaurant mapRow(ResultSet rs) throws SQLException {
        return new Restaurant(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("location"),
                rs.getInt("likes"),
                rs.getInt("dislikes"),
                rs.getInt("netScore"),
                rs.getInt("weeklyLikes"),
                rs.getString("logo")
        );
    }

    // Execute a query that returns a list of restaurants
    private static CompletableFuture<List<Restaurant>> queryList(String sql) {
        return CompletableFuture.supplyAsync(() -> {
            List<Restaurant> list = new ArrayList<>();
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            } catch (SQLException e) {
                throw new RuntimeException("Query failed: " + e.getMessage(), e);
            }
            return list;
        });
    }

    // Execute a raw SQL update statement
    private static void executeRaw(String sql, ThrowingConsumer<PreparedStatement> binder) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.accept(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL failed: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> { void accept(T t) throws SQLException; }
}

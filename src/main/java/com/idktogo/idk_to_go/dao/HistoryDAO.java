package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.core.DatabaseConnector;
import com.idktogo.idk_to_go.model.UserHistory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * HistoryDAO
 * Manages user like/dislike interactions with restaurants asynchronously.
 * Fully compatible with MySQL and Java record-based models.
 */
public final class HistoryDAO {

    private HistoryDAO() {}

    // === CREATE / UPDATE (UPSERT) ===
    public static CompletableFuture<Void> upsertInteraction(int userId, int restaurantId, Integer likedValue) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO user_history (user_id, restaurant_id, liked)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE liked = VALUES(liked), ts = CURRENT_TIMESTAMP
            """;

            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, restaurantId);

                if (likedValue == null) ps.setNull(3, Types.TINYINT);
                else ps.setInt(3, likedValue);

                ps.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to upsert user interaction: " + e.getMessage(), e);
            }
        });
    }

    // === READ ===
    public static CompletableFuture<Optional<Integer>> getInteractionType(int userId, int restaurantId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT liked FROM user_history WHERE user_id = ? AND restaurant_id = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, restaurantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int liked = rs.getInt("liked");
                        return rs.wasNull() ? Optional.empty() : Optional.of(liked);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch interaction type: " + e.getMessage(), e);
            }
            return Optional.empty();
        });
    }

    public static CompletableFuture<List<UserHistory>> listByUser(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM user_history WHERE user_id = ? ORDER BY ts DESC";
            List<UserHistory> list = new ArrayList<>();

            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to list user history: " + e.getMessage(), e);
            }
            return list;
        });
    }

    // === DELETE ===
    public static CompletableFuture<Boolean> deleteInteraction(int userId, int restaurantId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM user_history WHERE user_id = ? AND restaurant_id = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, restaurantId);
                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete user interaction: " + e.getMessage(), e);
            }
        });
    }

    public static CompletableFuture<Integer> deleteAllForUser(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM user_history WHERE user_id = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                return ps.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete all history for user: " + e.getMessage(), e);
            }
        });
    }

    public static CompletableFuture<Void> deleteAllForAllUsers() {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM user_history";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to clear all user history: " + e.getMessage(), e);
            }
        });
    }

    // === Utility Mapper ===
    private static UserHistory mapRow(ResultSet rs) throws SQLException {
        Integer liked = rs.getInt("liked");
        if (rs.wasNull()) liked = null;

        return new UserHistory(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("restaurant_id"),
                liked,
                rs.getTimestamp("ts").toLocalDateTime()
        );
    }
}

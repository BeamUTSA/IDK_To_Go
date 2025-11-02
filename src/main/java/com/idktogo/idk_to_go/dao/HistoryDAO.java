package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.data.SQLiteManager;
import com.idktogo.idk_to_go.model.UserHistory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HistoryDAO {

    private HistoryDAO() {}

    /**
     * Inserts or updates a like/dislike interaction.
     * - If an existing record exists, update it.
     * - Otherwise, create a new one.
     */
    public static boolean upsertInteraction(int userId, int restaurantId, Integer likedValue) {
        final String sql = """
            INSERT INTO user_history (user_id, restaurant_id, liked, timestamp)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(user_id, restaurant_id) DO UPDATE SET
                liked = excluded.liked,
                timestamp = CURRENT_TIMESTAMP
        """;

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restaurantId);
            ps.setObject(3, likedValue);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("HistoryDAO.upsertInteraction failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper to fetch current reaction for a (user, restaurant).
     * Returns:
     *   Optional.empty()  -> no row
     *   Optional.of(null) -> row exists but 'liked' is NULL (neutral)
     *   Optional.of(1)    -> liked
     *   Optional.of(-1)   -> disliked
     */
    public static Optional<Integer> getInteractionType(int userId, int restaurantId) {
        final String sql = "SELECT liked FROM user_history WHERE user_id = ? AND restaurant_id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Integer liked = (Integer) rs.getObject("liked"); // May be null
                return Optional.ofNullable(liked);
            }

        } catch (SQLException e) {
            System.err.println("HistoryDAO.getInteractionType failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves all user history records for a given user.
     */
    public static List<UserHistory> getHistoryForUser(int userId) {
        final String sql = """
            SELECT * FROM user_history
            WHERE user_id = ?
            ORDER BY timestamp DESC
        """;

        List<UserHistory> historyList = new ArrayList<>();

        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                historyList.add(mapRowToUserHistory(rs));
            }

        } catch (SQLException e) {
            System.err.println("HistoryDAO.getHistoryForUser failed: " + e.getMessage());
        }

        return historyList;
    }

    /**
     * Deletes all history records for a specific user.
     */
    public static boolean deleteHistoryForUser(int userId) {
        final String sql = "DELETE FROM user_history WHERE user_id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("HistoryDAO.deleteHistoryForUser failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes all user history records for all users.
     */
    public static boolean deleteHistoryForAllUsers() {
        final String sql = "DELETE FROM user_history";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("HistoryDAO.deleteHistoryForAllUsers failed: " + e.getMessage());
            return false;
        }
    }


    // === Helper to map a row to UserHistory ===
    private static UserHistory mapRowToUserHistory(ResultSet rs) throws SQLException {
        return new UserHistory(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("restaurant_id"),
                rs.getObject("liked", Integer.class), // supports null
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}

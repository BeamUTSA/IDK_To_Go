package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.data.AppStorage;
import com.idktogo.idk_to_go.data.SQLiteManager;
import com.idktogo.idk_to_go.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    private static final String USER_ID_KEY = "loggedInUserId";
    private static final String USERNAME_KEY = "loggedInUsername";

    private static Integer loggedInUserId = null;
    private static String loggedInUsername = null;

    // === CREATE USER === //
    public static boolean register(String username, String password, String email, String firstName, String lastName) {
        final String sql = """
            INSERT INTO users (username, password, email, first_name, last_name)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = SQLiteManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, firstName);
            ps.setString(5, lastName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("UserDAO.register failed: " + e.getMessage());
            return false;
        }
    }

    // === LOGIN SESSION HELPERS ===

    /** Sets current logged-in user info */
    public static void setLoggedInUser(int id, String username) {
        loggedInUserId = id;
        loggedInUsername = username;

        AppStorage.save(USER_ID_KEY, String.valueOf(id));
        AppStorage.save(USERNAME_KEY, username);
    }

    /** Returns current logged-in user ID, or null if no user logged in */
    public static Integer getLoggedInUserId() {
        if (loggedInUserId == null) {
            String storedId = AppStorage.load(USER_ID_KEY);
            if (storedId != null) {
                loggedInUserId = Integer.valueOf(storedId);
            }
        }
        return loggedInUserId;
    }

    /** Returns current logged-in username, or null if no user logged in */
    public static String getLoggedInUsername() {
        if (loggedInUsername == null) {
            loggedInUsername = AppStorage.load(USERNAME_KEY);
        }
        return loggedInUsername;
    }

    /** Removes user session info */
    public static void clearLoggedInUser() {
        loggedInUserId = null;
        loggedInUsername = null;

        AppStorage.remove(USER_ID_KEY);
        AppStorage.remove(USERNAME_KEY);
    }

    /** Returns true if a user is currently logged in */
    public static boolean isUserLoggedIn() {
        return getLoggedInUserId() != null;
    }

    public static boolean isCurrentUserAdmin() {
        Integer userId = getLoggedInUserId();
        if (userId == null) return false;

        final String sql = "SELECT is_admin FROM users WHERE id = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("is_admin") == 1;
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.isCurrentUserAdmin failed: " + e.getMessage());
        }

        return false;
    }


    // === READ === //
    public static Optional<User> getUserById(int userId) {
        final String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = SQLiteManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.getUserById failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<User> getUserByIdAndPassword(int userId, String password) {
        final String sql = "SELECT * FROM users WHERE id = ? AND password = ?";
        try (Connection conn = SQLiteManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.getUserByIdAndPassword failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<User> getUserByUsernameAndPassword(String username, String password) {
        final String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = SQLiteManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.getUserByUsernameAndPassword failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    // === UPDATE === //
    public static boolean updateEmail(int userId, String newEmail) {
        final String sql = "UPDATE users SET email = ? WHERE id = ?";
        return executeUpdate(sql, newEmail, userId);
    }

    public static boolean updatePassword(int userId, String newPassword) {
        final String sql = "UPDATE users SET password = ? WHERE id = ?";
        return executeUpdate(sql, newPassword, userId);
    }

    public static boolean updateName(int userId, String firstName, String lastName) {
        final String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE id = ?";
        return executeUpdate(sql, firstName, lastName, userId);
    }

    public static boolean updateUsername(int userId, String newUsername) {
        final String sql = "UPDATE users SET username = ? WHERE id = ?";
        return executeUpdate(sql, newUsername, userId);
    }

    // === DELETE === //
    public static boolean deleteUser(int userId) {
        final String sql = "DELETE FROM users WHERE id = ?";
        return executeUpdate(sql, userId);
    }

    // === VALIDATION HELPERS === //
    public static boolean isUsernameTaken(String username) {
        return exists("SELECT 1 FROM users WHERE username = ?", username);
    }

    public static boolean isEmailTaken(String email) {
        return exists("SELECT 1 FROM users WHERE email = ?", email);
    }

    // === PRIVATE HELPER METHODS === //
    private static boolean executeUpdate(String sql, Object... params) {
        try (Connection conn = SQLiteManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO executeUpdate failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean exists(String sql, String value) {
        try (Connection conn = SQLiteManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("UserDAO.exists failed: " + e.getMessage());
            return false;
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getTimestamp("created_at")
        );
    }
}

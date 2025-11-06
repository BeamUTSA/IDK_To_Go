package com.idktogo.idk_to_go.dao;

import com.idktogo.idk_to_go.core.DatabaseConnector;
import com.idktogo.idk_to_go.model.User;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class UserDAO {

    private UserDAO() {}

    // === CREATE ===
    public static CompletableFuture<Integer> create(User user) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO users (username, email, first_name, last_name, password, is_admin)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, user.username());
                ps.setString(2, user.email());
                ps.setString(3, user.firstName());
                ps.setString(4, user.lastName());
                ps.setString(5, user.password());
                ps.setBoolean(6, user.isAdmin());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
                return -1;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
            }
        });
    }

    // === READ ===
    public static CompletableFuture<Optional<User>> findById(int id) {
        return querySingle("SELECT * FROM users WHERE id = ?", ps -> ps.setInt(1, id));
    }

    public static CompletableFuture<Optional<User>> findByUsername(String username) {
        return querySingle("SELECT * FROM users WHERE username = ?", ps -> ps.setString(1, username));
    }

    public static CompletableFuture<Optional<User>> findByEmail(String email) {
        return querySingle("SELECT * FROM users WHERE email = ?", ps -> ps.setString(1, email));
    }

    public static CompletableFuture<Optional<User>> findByCredentials(String username, String password) {
        return querySingle("SELECT * FROM users WHERE username = ? AND password = ?",
                ps -> { ps.setString(1, username); ps.setString(2, password); });
    }

    public static CompletableFuture<List<User>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<User> list = new ArrayList<>();
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            } catch (SQLException e) {
                throw new RuntimeException("Failed to list users: " + e.getMessage(), e);
            }
            return list;
        });
    }

    // === UPDATE ===
    public static CompletableFuture<Boolean> update(User user) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                UPDATE users
                SET username = ?, email = ?, first_name = ?, last_name = ?, password = ?
                WHERE id = ?
            """;
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.username());
                ps.setString(2, user.email());
                ps.setString(3, user.firstName());
                ps.setString(4, user.lastName());
                ps.setString(5, user.password());
                ps.setInt(6, user.id());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
            }
        });
    }

    // === DELETE ===
    public static CompletableFuture<Boolean> delete(int id) {
        return executeUpdate("DELETE FROM users WHERE id = ?", ps -> ps.setInt(1, id));
    }

    // === UTIL ===
    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getBoolean("is_admin"),
                rs.getTimestamp("created_at")
        );
    }

    private static CompletableFuture<Optional<User>> querySingle(String sql, ThrowingConsumer<PreparedStatement> binder) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                binder.accept(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new RuntimeException("Query failed: " + e.getMessage(), e);
            }
        });
    }

    private static CompletableFuture<Boolean> executeUpdate(String sql, ThrowingConsumer<PreparedStatement> binder) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                binder.accept(ps);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("SQL update failed: " + e.getMessage(), e);
            }
        });
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }
}

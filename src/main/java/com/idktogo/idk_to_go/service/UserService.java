package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.model.User;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class UserService {
    private UserService() {}

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public static CompletableFuture<Void> register(User user) {
        if (user.username() == null || user.username().trim().isEmpty())
            return CompletableFuture.failedFuture(new IllegalArgumentException("Username cannot be blank"));
        if (user.email() == null || !EMAIL_PATTERN.matcher(user.email()).matches())
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid email format"));
        if (user.password() == null || user.password().length() < 6)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Password must be at least 6 characters"));

        return UserDAO.findByUsername(user.username()).thenCompose(opt -> {
            if (opt.isPresent())
                return CompletableFuture.failedFuture(new RuntimeException("Username already taken"));

            return UserDAO.findByEmail(user.email()).thenCompose(emailOpt -> {
                if (emailOpt.isPresent())
                    return CompletableFuture.failedFuture(new RuntimeException("Email already registered"));

                User newUser = new User(
                        0,
                        user.username(),
                        user.email(),
                        user.firstName(),
                        user.lastName(),
                        user.password(),
                        false,
                        Timestamp.from(Instant.now())
                );
                return UserDAO.create(newUser).thenApply(id -> null);
            });
        });
    }

    public static CompletableFuture<User> getByCredentials(String username, String password) {
        return UserDAO.findByCredentials(username, password)
                .thenApply(opt -> opt.orElseThrow(() -> new RuntimeException("Invalid username or password")));
    }

    public static CompletableFuture<Boolean> update(User updatedUser) {
        if (updatedUser.username() == null || updatedUser.username().trim().isEmpty())
            return CompletableFuture.failedFuture(new IllegalArgumentException("Username cannot be blank"));
        if (updatedUser.email() == null || !EMAIL_PATTERN.matcher(updatedUser.email()).matches())
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid email format"));
        if (updatedUser.password() == null || updatedUser.password().length() < 6)
            return CompletableFuture.failedFuture(new IllegalArgumentException("Password must be at least 6 characters"));

        return UserDAO.update(updatedUser);
    }

    public static CompletableFuture<Boolean> delete(int userId) {
        return UserDAO.delete(userId);
    }
}

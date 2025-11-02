package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.model.User;

import java.util.Optional;
import java.util.regex.Pattern;

public final class UserService {

    private UserService() {}

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    /** Fetch user profile as a model */
    public static Optional<User> getUserById(int userId) {
        return UserDAO.getUserById(userId);
    }

    /** Update user's email if new one is valid and not already taken */
    public static boolean updateEmail(int userId, String newEmail) {
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            System.err.println("Invalid email format");
            return false;
        }
        if (UserDAO.isEmailTaken(newEmail)) {
            System.err.println("Email is already registered");
            return false;
        }

        return UserDAO.updateEmail(userId, newEmail);
    }

    /** Change password if old one matches and new one is valid */
    public static boolean updatePassword(int userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = UserDAO.getUserByIdAndPassword(userId, oldPassword);

        if (userOpt.isEmpty()) {
            System.err.println("Current password is incorrect");
            return false;
        }
        if (newPassword == null || newPassword.length() < 6) {
            System.err.println("New password must be at least 6 characters");
            return false;
        }

        return UserDAO.updatePassword(userId, newPassword);
    }

    /** Update first and/or last name */
    public static boolean updateName(int userId, String firstName, String lastName) {
        return UserDAO.updateName(userId, firstName, lastName);
    }

    /** Update username if available */
    public static boolean updateUsername(int userId, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            System.err.println("Username cannot be empty");
            return false;
        }
        if (UserDAO.isUsernameTaken(newUsername)) {
            System.err.println("Username is already in use");
            return false;
        }
        return UserDAO.updateUsername(userId, newUsername);
    }

    /** Delete user and cascade delete their history and related data */
    public static boolean deleteAccount(int userId) {
        return UserDAO.deleteUser(userId);
    }
}

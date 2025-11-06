package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.SessionManager;
import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.model.User;
import com.idktogo.idk_to_go.service.HistoryService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class EditAccountController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private PasswordField passwordField;

    private final HistoryService historyService = new HistoryService();

    // ----------------------------------------
    // Navigation
    // ----------------------------------------
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }

    // ----------------------------------------
    // Account Management
    // ----------------------------------------
    @FXML
    private void saveChanges() {
        Integer userId = SessionManager.getUserId();
        if (userId == null) {
            showAlert("Error", "No user logged in.");
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String password = passwordField.getText().trim();

        UserDAO.findById(userId).thenCompose(optionalUser -> {
            if (optionalUser.isEmpty()) {
                Platform.runLater(() -> showAlert("Error", "User not found."));
                return CompletableFuture.completedFuture(false);
            }

            User existing = optionalUser.get();
            boolean anyChange = false;

            // Carry forward existing values
            String newUsername = existing.username();
            String newEmail = existing.email();
            String newFirst = existing.firstName();
            String newLast = existing.lastName();
            String newPassword = existing.password();

            // Update fields if non-empty
            if (!username.isBlank()) {
                newUsername = username;
                anyChange = true;
            }
            if (!email.isBlank()) {
                newEmail = email;
                anyChange = true;
            }
            if (!firstName.isBlank()) {
                newFirst = firstName;
                anyChange = true;
            }
            if (!lastName.isBlank()) {
                newLast = lastName;
                anyChange = true;
            }
            if (!password.isBlank()) {
                newPassword = password;
                anyChange = true;
            }

            if (!anyChange) {
                Platform.runLater(() -> showAlert("Info", "No changes to save."));
                return CompletableFuture.completedFuture(false);
            }

            // Create updated User record (record-based immutability)
            User updated = new User(
                    existing.id(),
                    newUsername,
                    newEmail,
                    newFirst,
                    newLast,
                    newPassword,
                    existing.isAdmin(),
                    Timestamp.from(Instant.now())
            );

            return UserDAO.update(updated).thenApply(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        showAlert("Success", "Account updated successfully.");
                        clearFields();
                    } else {
                        showAlert("Error", "Failed to update account.");
                    }
                });
                return success;
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("Error", "Update failed: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void clearHistory() {
        Integer userId = SessionManager.getUserId();
        if (userId == null) {
            showAlert("Error", "No user logged in.");
            return;
        }

        HistoryService.clearUserHistory(userId)
                .thenRun(() -> Platform.runLater(() -> showAlert("Success", "User history cleared.")))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Error", "Failed to clear history: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void deleteAccount() {
        Integer userId = SessionManager.getUserId();
        if (userId == null) {
            showAlert("Error", "No user logged in.");
            return;
        }

        UserDAO.delete(userId)
                .thenRun(() -> Platform.runLater(() -> {
                    showAlert("Deleted", "Account deleted successfully.");
                    SessionManager.logout();
                    Navigation.load("/com/idktogo/idk_to_go/login.fxml");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Error", "Failed to delete account: " + ex.getMessage()));
                    return null;
                });
    }

    // ----------------------------------------
    // Utility
    // ----------------------------------------
    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        firstNameField.clear();
        lastNameField.clear();
        passwordField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

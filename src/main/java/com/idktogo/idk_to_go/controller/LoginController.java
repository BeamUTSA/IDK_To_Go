package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.SessionManager;
import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.data.AppStorage;
import com.idktogo.idk_to_go.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        // === MySQL-based async authentication ===
        UserDAO.findByUsername(username)
                .thenAccept(optionalUser -> Platform.runLater(() -> {
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();

                        // (Optional) Replace with hashed password check if implemented
                        if (user.password().equals(password)) {

                            // ✅ Save session in persistent AppStorage
                            SessionManager.login(user.id(), user.username());

                            if (rememberMeCheck.isSelected()) {
                                AppStorage.save("rememberMe", "true");
                            } else {
                                AppStorage.remove("rememberMe");
                            }

                            showAlert("Welcome", "Login successful.");
                            Navigation.load("/com/idktogo/idk_to_go/main.fxml");
                        } else {
                            showAlert("Error", "Invalid username or password.");
                        }
                    } else {
                        showAlert("Error", "User not found.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Error", "Login failed: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void goToRegister() {
        Navigation.load("/com/idktogo/idk_to_go/register.fxml");
    }

    // Helper for alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

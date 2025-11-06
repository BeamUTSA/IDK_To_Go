package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.SessionManager;
import com.idktogo.idk_to_go.dao.UserDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class OptionsController {

    @FXML private Button adminButton;

    @FXML
    private void initialize() {
        adminButton.setVisible(false); // Hide until user verified

        Integer userId = SessionManager.getUserId();
        if (userId != null) {
            UserDAO.findById(userId).thenAccept(optionalUser -> {
                optionalUser.ifPresent(user -> {
                    boolean isAdmin = user.isAdmin();
                    Platform.runLater(() -> adminButton.setVisible(isAdmin));
                });
            }).exceptionally(ex -> {
                System.err.println("Failed to check admin status: " + ex.getMessage());
                return null;
            });
        }
    }

    // ----------------------------------------
    // Navigation
    // ----------------------------------------

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }

    @FXML
    private void openEditAccount() {
        Navigation.load("/com/idktogo/idk_to_go/edit_account.fxml");
    }

    @FXML
    private void openAppearanceSettings() {
        Navigation.load("/com/idktogo/idk_to_go/appearance.fxml");
    }

    @FXML
    private void openAboutApp() {
        Navigation.load("/com/idktogo/idk_to_go/about_app.fxml");
    }

    @FXML
    private void openAdmin() {
        Navigation.load("/com/idktogo/idk_to_go/admin.fxml");
    }

    @FXML
    private void logout() {
        SessionManager.logout();
        Navigation.load("/com/idktogo/idk_to_go/login.fxml");
    }
}

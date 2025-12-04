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
        adminButton.setVisible(false); // Hide until user is verified as admin

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

    // Navigate back to the main scene
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }

    // Open the edit account scene
    @FXML
    private void openEditAccount() {
        Navigation.load("/com/idktogo/idk_to_go/edit_account.fxml");
    }

    // Open appearance settings
    @FXML
    private void openAppearanceSettings() {
        Navigation.load("/com/idktogo/idk_to_go/appearance.fxml");
    }

    // Open the about-app scene
    @FXML
    private void openAboutApp() {
        Navigation.load("/com/idktogo/idk_to_go/about_app.fxml");
    }

    // Open the admin panel
    @FXML
    private void openAdmin() {
        Navigation.load("/com/idktogo/idk_to_go/admin.fxml");
    }

    // Log out the current user and go to login scene
    @FXML
    private void logout() {
        SessionManager.logout();
        Navigation.load("/com/idktogo/idk_to_go/login.fxml");
    }
}

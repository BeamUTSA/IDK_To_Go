package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class OptionsController {

    @FXML private Button adminButton;

    @FXML
    private void initialize() {
        // Only show admin button if user has admin role
        adminButton.setVisible(UserDAO.isCurrentUserAdmin());
    }

    // ----------------------------------------
    // Navigation methods
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
        UserDAO.clearLoggedInUser(); // Clear session
        Navigation.load("/com/idktogo/idk_to_go/login.fxml"); // Back to login
    }
}

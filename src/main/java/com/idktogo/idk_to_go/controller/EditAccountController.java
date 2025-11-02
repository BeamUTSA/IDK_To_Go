package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.service.UserHistoryService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EditAccountController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private PasswordField passwordField;

    private final UserHistoryService historyService = UserHistoryService.getInstance();
    private final int userId = UserDAO.getLoggedInUserId();

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
        boolean updated = false;

        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            UserDAO.updateUsername(userId, username);
            updated = true;
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty()) {
            UserDAO.updateEmail(userId, email);
            updated = true;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            UserDAO.updateName(userId, firstName, lastName);
            updated = true;
        }

        String password = passwordField.getText().trim();
        if (!password.isEmpty()) {
            UserDAO.updatePassword(userId, password);
            updated = true;
        }

        System.out.println(updated ? "Account updated successfully." : "No changes to save.");
    }

    @FXML
    private void clearHistory() {
        historyService.clearUserHistory(userId);
        System.out.println("User history cleared.");
    }

    @FXML
    private void deleteAccount() {
        UserDAO.deleteUser(userId);
        UserDAO.clearLoggedInUser();
        Navigation.load("/com/idktogo/idk_to_go/login.fxml");
    }
}

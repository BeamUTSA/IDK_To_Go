package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleRegister() {
        // Collect input
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill out all required fields.");
            return;
        }

        if (UserDAO.isUsernameTaken(username)) {
            System.out.println("Username is already taken.");
            return;
        }

        if (UserDAO.isEmailTaken(email)) {
            System.out.println("Email is already taken.");
            return;
        }

        boolean success = UserDAO.register(username, password, email, firstName, lastName);
        if (success) {
            System.out.println("Registration successful. Redirecting to login...");
            goToLogin();
        } else {
            System.out.println("Registration failed.");
        }
    }

    @FXML
    private void goToLogin() {
        Navigation.load("/com/idktogo/idk_to_go/login.fxml");
    }
}

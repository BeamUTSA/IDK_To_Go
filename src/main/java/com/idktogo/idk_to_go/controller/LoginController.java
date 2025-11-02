package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.SessionManager;
import com.idktogo.idk_to_go.dao.UserDAO;
import com.idktogo.idk_to_go.data.AppStorage;
import com.idktogo.idk_to_go.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        Optional<User> user = UserDAO.getUserByUsernameAndPassword(username, password);
        if (user.isPresent()) {
            User loggedInUser = user.get();
            SessionManager.login(loggedInUser.id(), loggedInUser.username());

            if (rememberMeCheck.isSelected()) {
                AppStorage.save("rememberMe", "true");
            } else {
                AppStorage.remove("rememberMe");
            }

            Navigation.load("/com/idktogo/idk_to_go/main.fxml");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    @FXML
    private void goToRegister() {
        Navigation.load("/com/idktogo/idk_to_go/register.fxml");
    }
}

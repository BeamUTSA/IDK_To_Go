package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class AppearanceController {

    @FXML
    private ToggleButton lightModeToggle;

    @FXML
    private ToggleButton darkModeToggle;

    private Stage stage;

    public void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        lightModeToggle.setToggleGroup(toggleGroup);
        darkModeToggle.setToggleGroup(toggleGroup);

        // Set the correct toggle button based on the active theme
        if (ThemeManager.getActiveTheme() == ThemeManager.Theme.DARK) {
            darkModeToggle.setSelected(true);
        } else {
            lightModeToggle.setSelected(true);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Switch to light mode
    @FXML
    private void switchToLightMode() {
        applyTheme(ThemeManager.Theme.LIGHT);
    }

    // Switch to dark mode
    @FXML
    private void switchToDarkMode() {
        applyTheme(ThemeManager.Theme.DARK);
    }

    // Apply the selected theme to the scene
    private void applyTheme(ThemeManager.Theme theme) {
        ThemeManager.setActiveTheme(theme);

        Scene scene = lightModeToggle.getScene();
        if (scene != null) {
            ThemeManager.applyTheme(scene);
        }
    }

    // Navigate back to the options scene
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }
}

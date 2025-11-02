package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.util.NavigableMap;

public class AppearanceController {

    @FXML
    private ToggleButton lightModeToggle;

    @FXML
    private ToggleButton darkModeToggle;

    private Stage stage;

    public void initialize() {
        // Assign ToggleGroup programmatically
        ToggleGroup toggleGroup = new ToggleGroup();
        lightModeToggle.setToggleGroup(toggleGroup);
        darkModeToggle.setToggleGroup(toggleGroup);

        // Reflect current active theme on UI
        if (ThemeManager.getActiveTheme() == ThemeManager.Theme.DARK) {
            darkModeToggle.setSelected(true);
        } else {
            lightModeToggle.setSelected(true);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void switchToLightMode() {
        applyTheme(ThemeManager.Theme.LIGHT);
    }

    @FXML
    private void switchToDarkMode() {
        applyTheme(ThemeManager.Theme.DARK);
    }

    private void applyTheme(ThemeManager.Theme theme) {
        ThemeManager.setActiveTheme(theme);

        // Fetch Scene from any visible UI component
        Scene scene = lightModeToggle.getScene();
        if (scene != null) {
            ThemeManager.applyTheme(scene);
        }
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }
}

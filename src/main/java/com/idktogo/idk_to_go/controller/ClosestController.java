package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import javafx.fxml.FXML;

public class ClosestController {

    @FXML
    private void openMaps() {
        try {
            java.awt.Desktop.getDesktop().browse(
                    java.net.URI.create("https://www.google.com/maps/search/restaurants+near+me/")
            );
        } catch (Exception e) {
            System.err.println("Failed to open Google Maps: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }
}

package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import javafx.fxml.FXML;

public class AboutAppController {

    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/options.fxml");
    }
}

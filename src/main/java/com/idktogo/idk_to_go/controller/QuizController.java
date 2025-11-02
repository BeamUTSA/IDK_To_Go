package com.idktogo.idk_to_go.controller;

import com.idktogo.idk_to_go.core.Navigation;
import javafx.fxml.FXML;

public class QuizController {

    /**
     * Return to the main screen.
     */
    @FXML
    private void goBack() {
        Navigation.load("/com/idktogo/idk_to_go/main.fxml");
    }

    /**
     * Placeholder for future AI-based quiz generation.
     */
    @FXML
    private void generateQuiz() {
        System.out.println("Quiz generation triggered... (not implemented yet)");
        // TODO: Implement AI quiz generation using backend or cloud service
    }
}

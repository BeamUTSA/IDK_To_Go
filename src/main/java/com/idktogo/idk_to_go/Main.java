package com.idktogo.idk_to_go;

import com.idktogo.idk_to_go.core.DatabaseConnector;
import com.idktogo.idk_to_go.core.Navigation;
import com.idktogo.idk_to_go.core.SessionManager;
import com.idktogo.idk_to_go.core.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Initialize MySQL connection via Railway
            DatabaseConnector.getConnection();

            // Setup app window
            Navigation.setStage(stage);

            String fxmlPath = SessionManager.isLoggedIn()
                    ? "/com/idktogo/idk_to_go/main.fxml"
                    : "/com/idktogo/idk_to_go/login.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            ThemeManager.applyTheme(scene);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("IDK To-Go");
            stage.show();

            System.out.println("Application started successfully!");

        } catch (Exception e) {
            System.err.println("Startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

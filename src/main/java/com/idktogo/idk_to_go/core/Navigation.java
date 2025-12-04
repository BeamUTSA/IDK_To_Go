package com.idktogo.idk_to_go.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages JavaFX scene navigation using a single primary stage.
 * Applies themes and injects the stage into controllers.
 */
public final class Navigation {

    // The main application window
    private static Stage appStage;

    private Navigation() {}

    /**
     * Sets the primary stage for the application.
     * @param stage The main application window.
     */
    public static void setStage(Stage stage) {
        Navigation.appStage = stage;
    }

    /**
     * Gets the primary stage.
     * @return The primary stage.
     */
    public static Stage getStage() {
        if (appStage == null) {
            throw new IllegalStateException("Stage has not been set. Call Navigation.setStage(stage) first.");
        }
        return appStage;
    }

    /**
     * Loads an FXML scene.
     * @param absoluteFxmlPath Classpath to the FXML file (e.g., "/com/idktogo/idk_to_go/main.fxml").
     */
    public static void load(String absoluteFxmlPath) {
        load(absoluteFxmlPath, null);
    }

    /**
     * Loads an FXML scene with an optional controller configuration.
     * @param absoluteFxmlPath Classpath to the FXML file.
     * @param controllerConfigurator Optional function to configure the controller.
     */
    public static void load(String absoluteFxmlPath, Consumer<Object> controllerConfigurator) {
        Objects.requireNonNull(absoluteFxmlPath, "FXML path cannot be null");

        if (appStage == null) {
            throw new IllegalStateException("Stage not set. Call Navigation.setStage(stage) before navigating.");
        }

        try {
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(absoluteFxmlPath));
            if (loader.getLocation() == null) {
                throw new IllegalArgumentException("Invalid FXML path: " + absoluteFxmlPath);
            }

            Scene scene = new Scene(loader.load());
            ThemeManager.applyTheme(scene);
            appStage.setScene(scene);

            Object controller = loader.getController();
            injectStageIfPresent(controller);

            if (controllerConfigurator != null) {
                controllerConfigurator.accept(controller);
            }

            appStage.show();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + absoluteFxmlPath, e);
        }
    }

    /**
     * Injects the stage into controllers that have a `setStage(Stage)` method.
     */
    private static void injectStageIfPresent(Object controller) {
        if (controller == null) {
            return;
        }

        try {
            controller.getClass().getMethod("setStage", Stage.class).invoke(controller, appStage);
        } catch (NoSuchMethodException e) {
            // No setStage(Stage) method found, which is acceptable.
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject stage into controller: " + controller.getClass().getName(), e);
        }
    }
}

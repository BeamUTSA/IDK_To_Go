package com.idktogo.idk_to_go.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Centralized navigation utility for loading JavaFX scenes with a single stage.
 * Handles applying the current theme and injecting stage into controllers
 * that define a setStage(Stage) method.
 */
public final class Navigation {

    // Holds a global reference to the primary Stage
    private static Stage appStage;

    private Navigation() {}

    /**
     * Registers the main Stage to be used by subsequent navigation calls.
     *
     * @param stage global application window
     */
    public static void setStage(Stage stage) {
        Navigation.appStage = stage;
    }

    /**
     * Retrieves the currently registered Stage.
     * @return primary Stage
     */
    public static Stage getStage() {
        if (appStage == null) {
            throw new IllegalStateException("Stage has not been set. Call Navigation.setStage(stage) first.");
        }
        return appStage;
    }

    /**
     * Loads an FXML scene using the globally registered Stage.
     * The controller is automatically provided with stage access, if a setStage(Stage) method exists.
     *
     * @param absoluteFxmlPath classpath to FXML, e.g. "/com/idktogo/idk_to_go/main.fxml"
     */
    public static void load(String absoluteFxmlPath) {
        load(absoluteFxmlPath, null);
    }

    /**
     * Loads an FXML scene with controller configuration support.
     *
     * @param absoluteFxmlPath classpath to FXML
     * @param controllerConfigurator optional lambda to further configure the loaded controller
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
     * Injects a stage into controllers that implement a public void setStage(Stage) method.
     */
    private static void injectStageIfPresent(Object controller) {
        if (controller == null) {
            return;
        }

        try {
            controller.getClass().getMethod("setStage", Stage.class).invoke(controller, appStage);
        } catch (NoSuchMethodException e) {
            // Controller does not support stage injection, this is fine
            System.out.println("ℹ️ No setStage(Stage) found in " + controller.getClass().getSimpleName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject stage into controller: " + controller.getClass().getName(), e);
        }
    }
}

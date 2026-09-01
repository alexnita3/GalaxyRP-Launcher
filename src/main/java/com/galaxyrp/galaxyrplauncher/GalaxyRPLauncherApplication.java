package com.galaxyrp.galaxyrplauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GalaxyRPLauncherApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        String screen;
        if (isGameDataFolderPresent()) {
            screen = "galaxy.fxml";
        } else {
            screen = "errorScreen.fxml";
        }
        FXMLLoader fxmlLoader = new FXMLLoader(
                GalaxyRPLauncherApplication.class.getResource(screen));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("GalaxyRP Launcher");
        stage.setScene(scene);
        stage.show();
    }

    private boolean isGameDataFolderPresent() {
        Path gameDataFolder = Paths.get("GameData");
        return Files.isDirectory(gameDataFolder);
    }
}

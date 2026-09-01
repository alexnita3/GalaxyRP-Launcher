package com.galaxyrp.galaxyrplauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GalaxyRPLauncherApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GalaxyRPLauncherApplication.class.getResource("galaxy.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("GalaxyRP Launcher");
        stage.setScene(scene);
        stage.show();
    }
}

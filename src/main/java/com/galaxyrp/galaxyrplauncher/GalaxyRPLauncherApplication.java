package com.galaxyrp.galaxyrplauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        Parent content = fxmlLoader.load();
        AnchorPane titleBar = new AnchorPane();
        double windowWidth = content.prefWidth(-1);
        titleBar.setMinWidth(windowWidth);
        titleBar.setPrefWidth(windowWidth);
        titleBar.setMaxWidth(windowWidth);
        titleBar.setMinHeight(36);
        titleBar.setPrefHeight(36);
        titleBar.setMaxHeight(36);

        ImageView titleBarBackground = new ImageView(new Image(
                GalaxyRPLauncherApplication.class.getResource("galaxy_background.png").toExternalForm()));
        titleBarBackground.setFitWidth(windowWidth);
        titleBarBackground.setFitHeight(518);
        Rectangle backgroundClip = new Rectangle(windowWidth, 36);
        backgroundClip.widthProperty().bind(titleBar.widthProperty());
        titleBarBackground.setClip(backgroundClip);
        titleBarBackground.fitWidthProperty().bind(titleBar.widthProperty());
        AnchorPane.setTopAnchor(titleBarBackground, 0.0);
        AnchorPane.setLeftAnchor(titleBarBackground, 0.0);

        Label title = new Label("GalaxyRP Launcher");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        AnchorPane.setLeftAnchor(title, 14.0);
        AnchorPane.setTopAnchor(title, 10.0);

        HBox windowControls = new HBox(4,
                createWindowButton("−", "Minimize", () -> stage.setIconified(true)),
                createWindowButton("×", "Close", stage::close));
        windowControls.setAlignment(Pos.CENTER);
        AnchorPane.setRightAnchor(windowControls, 0.0);
        AnchorPane.setTopAnchor(windowControls, 0.0);
        titleBar.getChildren().addAll(titleBarBackground, title, windowControls);

        VBox windowRoot = new VBox(titleBar, content);
        windowRoot.setMinWidth(windowWidth);
        windowRoot.setPrefWidth(windowWidth);
        windowRoot.setMaxWidth(windowWidth);
        Scene scene = new Scene(windowRoot);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setTitle("GalaxyRP Launcher");
        stage.setScene(scene);
        enableWindowDragging(titleBar, stage);
        stage.show();
    }

    private Button createWindowButton(String text, String accessibleText,
                                      Runnable action) {
        Button button = new Button(text);
        button.setAccessibleText(accessibleText);
        button.setFocusTraversable(false);
        button.setMinSize(30, 24);
        button.setPrefSize(30, 24);
        button.setStyle("-fx-background-color: rgba(30, 30, 30, 0.55);"
                + "-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 0;"
                + "-fx-background-radius: 3;");
        button.setOnAction(event -> action.run());
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: rgba(80, 80, 80, 0.85);"
                + "-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 0;"
                + "-fx-background-radius: 3;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: rgba(30, 30, 30, 0.55);"
                + "-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 0;"
                + "-fx-background-radius: 3;"));
        return button;
    }

    private void enableWindowDragging(AnchorPane titleBar, Stage stage) {
        double[] dragOffset = new double[2];
        boolean[] dragging = new boolean[1];
        titleBar.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            dragging[0] = event.getButton() == MouseButton.PRIMARY
                    && !isControlTarget(event.getTarget(), titleBar);
            if (dragging[0]) {
                dragOffset[0] = event.getScreenX() - stage.getX();
                dragOffset[1] = event.getScreenY() - stage.getY();
            }
        });
        titleBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (dragging[0]) {
                stage.setX(event.getScreenX() - dragOffset[0]);
                stage.setY(event.getScreenY() - dragOffset[1]);
            }
        });
        titleBar.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> dragging[0] = false);
    }

    private boolean isControlTarget(Object target, Node windowRoot) {
        if (!(target instanceof Node node)) {
            return false;
        }
        while (node != null && node != windowRoot) {
            if (node instanceof Control) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private boolean isGameDataFolderPresent() {
        Path gameDataFolder = Paths.get("base");
        return Files.isDirectory(gameDataFolder);
    }
}

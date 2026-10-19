package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.adapters.GoogleDriveAdapter;
import javafx.application.Platform;

public class NewsService {
    private static final String NO_DRIVE_MESSAGE =
            "Set up Google Drive to get server news.";
    private static final String NO_NEWS_MESSAGE =
            "No news found. Talk to your server admin to upload a news.txt file.";

    private final GalaxyRPLauncherController controller;

    public NewsService(GalaxyRPLauncherController controller) {
        this.controller = controller;
    }

    public void loadNews() {
        String googleDriveLink = controller.googleDriveLinkTextBox.getText();
        if (googleDriveLink == null || googleDriveLink.isBlank()) {
            controller.serverNewsTextArea.setText(NO_DRIVE_MESSAGE);
            return;
        }

        Thread newsLoader = new Thread(() -> {
            try {
                String news = GoogleDriveAdapter
                        .readTextFileFromFolder(googleDriveLink, "news.txt")
                        .orElse(NO_NEWS_MESSAGE);
                Platform.runLater(() -> controller.serverNewsTextArea.setText(news));
            } catch (Exception exception) {
                String errorMessage = "Unable to load server news: " + exception.getMessage();
                Platform.runLater(() -> controller.serverNewsTextArea.setText(errorMessage));
            }
        }, "load-server-news");
        newsLoader.setDaemon(true);
        newsLoader.start();
    }
}

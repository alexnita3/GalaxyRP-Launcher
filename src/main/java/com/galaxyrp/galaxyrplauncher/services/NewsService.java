package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.adapters.GoogleDriveAdapter;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.parser.Parser;
import javafx.application.Platform;

import java.util.List;

public class NewsService {
    private static final String NO_DRIVE_MESSAGE =
            "Set up Google Drive to get server news.";
    private static final String NO_NEWS_MESSAGE =
            "No news found. Talk to your server admin to upload a news.txt file.";
    private static final Extension STRIKETHROUGH = StrikethroughExtension.create();
    private static final Parser MARKDOWN_PARSER = Parser.builder()
            .extensions(List.of(STRIKETHROUGH))
            .build();

    private final GalaxyRPLauncherController controller;
    private final NewsMarkdownRenderer markdownRenderer;

    public NewsService(GalaxyRPLauncherController controller) {
        this.controller = controller;
        this.markdownRenderer = new NewsMarkdownRenderer(controller.serverNewsScrollPane);
    }

    public void loadNews() {
        String googleDriveLink = controller.googleDriveLinkTextBox.getText();
        if (googleDriveLink == null || googleDriveLink.isBlank()) {
            displayNews(NO_DRIVE_MESSAGE);
            return;
        }

        Thread newsLoader = new Thread(() -> {
            try {
                String news = GoogleDriveAdapter
                        .readTextFileFromFolder(googleDriveLink, "news.txt")
                        .orElse(NO_NEWS_MESSAGE);
                displayNews(news);
            } catch (Exception exception) {
                String errorMessage = "Unable to load server news: " + exception.getMessage();
                displayNews(errorMessage);
            }
        }, "load-server-news");
        newsLoader.setDaemon(true);
        newsLoader.start();
    }

    private void displayNews(String markdown) {
        Platform.runLater(() ->
                markdownRenderer.render(MARKDOWN_PARSER.parse(markdown)));
    }
}

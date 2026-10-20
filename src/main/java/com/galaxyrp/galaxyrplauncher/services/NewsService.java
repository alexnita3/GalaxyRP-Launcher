package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.adapters.GoogleDriveAdapter;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NewsService {
    private static final String NO_DRIVE_MESSAGE =
            "Set up Google Drive to get server news.";
    private static final String NO_NEWS_MESSAGE =
            "No news found. Talk to your server admin to upload a news.txt file.";
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder()
            .escapeHtml(true)
            .build();
    private static final String NEWS_STYLES = loadNewsStyles();

    private final GalaxyRPLauncherController controller;

    public NewsService(GalaxyRPLauncherController controller) {
        this.controller = controller;
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
        String renderedMarkdown = HTML_RENDERER.render(MARKDOWN_PARSER.parse(markdown));
        String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<style>" + NEWS_STYLES + "</style>"
                + "</head><body>"
                + renderedMarkdown
                + "</body></html>";
        Platform.runLater(() -> controller.serverNewsWebView.getEngine().loadContent(html));
    }

    private static String loadNewsStyles() {
        String stylesheetPath = "/com/galaxyrp/galaxyrplauncher/services/news.css";
        try (InputStream stylesheet = NewsService.class.getResourceAsStream(stylesheetPath)) {
            if (stylesheet == null) {
                throw new IllegalStateException("News stylesheet not found: " + stylesheetPath);
            }
            return new String(stylesheet.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read news stylesheet: " + stylesheetPath, exception);
        }
    }
}

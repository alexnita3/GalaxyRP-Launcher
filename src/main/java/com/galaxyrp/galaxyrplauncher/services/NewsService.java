package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.adapters.GoogleDriveAdapter;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import javafx.application.Platform;

public class NewsService {
    private static final String NO_DRIVE_MESSAGE =
            "Set up Google Drive to get server news.";
    private static final String NO_NEWS_MESSAGE =
            "No news found. Talk to your server admin to upload a news.txt file.";
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder()
            .escapeHtml(true)
            .build();
    private static final String NEWS_STYLES = """
            <style>
              body {
                background-color: #2b2b2b;
                color: white;
                font-family: sans-serif;
                margin: 12px 28px 12px 12px;
                overflow-wrap: anywhere;
              }
              pre {
                white-space: pre-wrap;
                overflow-wrap: anywhere;
              }
              img {
                max-width: 100%;
                max-height: calc(100vh - 24px);
                width: auto;
                height: auto;
                object-fit: contain;
              }
              a { color: #8ab4f8; }
              blockquote {
                border-left: 3px solid #888;
                margin-left: 0;
                padding-left: 12px;
                color: #ccc;
              }
              table { border-collapse: collapse; }
              th, td { border: 1px solid #888; padding: 4px 8px; }
            </style>
            """;

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
                + NEWS_STYLES
                + "</head><body>"
                + renderedMarkdown
                + "</body></html>";
        Platform.runLater(() -> controller.serverNewsWebView.getEngine().loadContent(html));
    }
}

package com.galaxyrp.galaxyrplauncher;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DownloadProgress {
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final Button downloadButton;
    private long startTime;

    public DownloadProgress(
            ProgressBar progressBar, Label statusLabel, Button downloadButton) {
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
        this.downloadButton = downloadButton;
    }

    public void begin() {
        startTime = System.nanoTime();
        Platform.runLater(() -> {
            if (progressBar != null) {
                progressBar.setProgress(0);
                progressBar.setVisible(true);
            }
            if (statusLabel != null) {
                statusLabel.setText("Starting download...");
            }
            if (downloadButton != null) {
                downloadButton.setDisable(true);
            }
        });
    }

    public void update(long downloadedBytes, long totalBytes) {
        double progress = totalBytes > 0
                ? (double) downloadedBytes / totalBytes
                : 0;
        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double bytesPerSecond = elapsedSeconds > 0
                ? downloadedBytes / elapsedSeconds
                : 0;
        String status = totalBytes > 0
                ? String.format("%.0f%% - %.2f MB/s", progress * 100, bytesPerSecond / 1_048_576)
                : String.format("%.2f MB/s", bytesPerSecond / 1_048_576);

        Platform.runLater(() -> {
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
            if (statusLabel != null) {
                statusLabel.setText(status);
            }
        });
    }

    public void complete() {
        Platform.runLater(() -> {
            if (progressBar != null) {
                progressBar.setProgress(1);
            }
            if (statusLabel != null) {
                statusLabel.setText("100% - Complete");
            }
            if (downloadButton != null) {
                downloadButton.setDisable(false);
            }
        });
    }

    public void failed() {
        Platform.runLater(() -> {
            if (progressBar != null) {
                progressBar.setProgress(0);
            }
            if (statusLabel != null) {
                statusLabel.setText("Download failed");
            }
            if (downloadButton != null) {
                downloadButton.setDisable(false);
            }
        });
    }
}

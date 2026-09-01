package com.galaxyrp.galaxyrplauncher.services;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DownloadProgressService {
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final Button downloadButton;
    private long startTime;

    public DownloadProgressService(
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
        });
    }

    public void update(long downloadedBytes, long totalBytes) {
        double progress;
        if (totalBytes > 0) {
            progress = (double) downloadedBytes / totalBytes;
        } else {
            progress = 0;
        }

        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double bytesPerSecond;
        if (elapsedSeconds > 0) {
            bytesPerSecond = downloadedBytes / elapsedSeconds;
        } else {
            bytesPerSecond = 0;
        }

        String status;
        if (totalBytes > 0) {
            status = String.format(
                    "%.0f%% - %.2f MB/s", progress * 100, bytesPerSecond / 1_048_576);
        } else {
            status = String.format("%.2f MB/s", bytesPerSecond / 1_048_576);
        }

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
        });
    }
}

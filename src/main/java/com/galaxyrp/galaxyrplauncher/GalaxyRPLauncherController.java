package com.galaxyrp.galaxyrplauncher;

import com.google.api.services.drive.model.File;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

public class GalaxyRPLauncherController {
    public TextField server1IpTextBox;
    public TextField server1NameTextBox;
    public TextField server2IpTextBox;
    public TextField server2NameTextBox;
    public TextField googleDriveLinkTextBox;
    public TextField resolutionXTextBox;
    public TextField customArgumentsTextBox;
    public ChoiceBox clientModDropDown;
    public CheckBox scanOnStartCheckBox;
    public TextField ResolutionYTestBox;
    public CheckBox automaticallyDownloadCheckBox;
    public Label fileNameLabel;
    public Label fileSizeLabel;
    public Label fileVersionNumberLabel;
    public Label fileLastChangedLabel;
    public Button downloadSelectedButton;
    public Button checkUpdateButton;
    public Button downloadAllButton;
    public Button launchGameButton;
    public ChoiceBox serverSelectDropDownList;
    public ListView<String> cloudFileList;
    public ProgressBar downloadProgressBar;
    public Label downloadStatusLabel;

    List<File> googleDriveFiles;

    @FXML
    public void onDownloadAllButtonClick() {
        System.out.println("Download All Button clicked");
    }

    @FXML
    public void displayFileDetails() {
        if (cloudFileList.getSelectionModel().getSelectedIndex() != -1) {
            int index = cloudFileList.getSelectionModel().getSelectedIndex();

            File selectedFile = googleDriveFiles.get(index);

            HelperMethods.updateFileDetailLabels(this, selectedFile);
        }
    }

    @FXML
    public void onCheckUpdateButtonClick() {
        Thread driveThread = new Thread(() -> {
            try {
                String folderLink = googleDriveLinkTextBox != null ? googleDriveLinkTextBox.getText() : null;
                googleDriveFiles =
                        folderLink == null || folderLink.isBlank()
                                ? DriveQuickstart.listFilesFromFolder(googleDriveLinkTextBox.getText())
                                : DriveQuickstart.listFilesFromFolder(folderLink);

                List<String> fileNames = googleDriveFiles.stream()
                        .map(com.google.api.services.drive.model.File::getName)
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    if (cloudFileList != null) {
                        cloudFileList.getItems().setAll(fileNames);
                    }
                });
            } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (cloudFileList != null) {
                        cloudFileList.getItems().clear();
                        cloudFileList.getItems().add("Google Drive error: " + e.getMessage());
                    }
                });
            }
        });
        driveThread.setDaemon(true);
        driveThread.start();
    }

    @FXML
    public void onDownloadSelectedButtonClick() {
        int selectedIndex = cloudFileList.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1 || googleDriveFiles == null
                || selectedIndex >= googleDriveFiles.size()) {
            return;
        }

        File selectedFile = googleDriveFiles.get(selectedIndex);
        String fileName = selectedFile.getName();
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        if (downloadProgressBar != null) {
            downloadProgressBar.setProgress(0);
            downloadProgressBar.setVisible(true);
        }
        if (downloadSelectedButton != null) {
            downloadSelectedButton.setDisable(true);
        }
        if (downloadStatusLabel != null) {
            downloadStatusLabel.setText("Starting download...");
        }

        Thread downloadThread = new Thread(() -> {
            try {
                Path destination = Paths.get("downloads", sanitizeFileName(fileName));
                long startTime = System.nanoTime();
                DriveQuickstart.downloadFileWithProgress(selectedFile.getId(), destination,
                        (downloadedBytes, totalBytes) -> {
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
                        if (downloadProgressBar != null) {
                            downloadProgressBar.setProgress(progress);
                        }
                        if (downloadStatusLabel != null) {
                            downloadStatusLabel.setText(status);
                        }
                    });
                });
                Platform.runLater(() -> {
                    if (downloadProgressBar != null) {
                        downloadProgressBar.setProgress(1);
                    }
                    if (downloadStatusLabel != null) {
                        downloadStatusLabel.setText("100% - Complete");
                    }
                    if (downloadSelectedButton != null) {
                        downloadSelectedButton.setDisable(false);
                    }
                });
            } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (downloadProgressBar != null) {
                        downloadProgressBar.setProgress(0);
                    }
                    if (downloadStatusLabel != null) {
                        downloadStatusLabel.setText("Download failed");
                    }
                    if (downloadSelectedButton != null) {
                        downloadSelectedButton.setDisable(false);
                    }
                    if (cloudFileList != null) {
                        cloudFileList.getItems().clear();
                        cloudFileList.getItems().add("Google Drive error: " + e.getMessage());
                    }
                });
            }
        });
        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

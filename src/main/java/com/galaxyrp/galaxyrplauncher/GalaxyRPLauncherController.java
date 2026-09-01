package com.galaxyrp.galaxyrplauncher;

import com.galaxyrp.galaxyrplauncher.exceptions.LauncherErrorException;
import com.galaxyrp.galaxyrplauncher.services.DownloadProgressService;
import com.galaxyrp.galaxyrplauncher.services.GoogleDriveService;
import com.galaxyrp.galaxyrplauncher.services.InterfaceUpdateService;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import com.google.api.services.drive.model.File;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    public Label statusLabel;

    public List<File> googleDriveFiles;

    @FXML
    public void onDownloadAllButtonClick() {
        System.out.println("Download All Button clicked");

        if (googleDriveFiles == null || googleDriveFiles.isEmpty()) {
            InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
            return;
        }

        InterfaceUpdateService.updateUserInterface(this, UserAction.PRESSED_DOWNLOAD_ALL);
        DownloadProgressService progressView =
                new DownloadProgressService(downloadProgressBar, downloadStatusLabel, downloadAllButton);
        progressView.begin();

        Thread downloadAllThread = new Thread(() -> {
            try {
                GoogleDriveService.downloadFilesWithProgress(
                        googleDriveFiles, Paths.get("downloads"), progressView::update);
                progressView.complete();
                InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
            } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
                progressView.failed();
                InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
                throw new LauncherErrorException(e.getMessage(), "download all", this);
            }
        }, "google-drive-download-all");
        downloadAllThread.setDaemon(true);
        downloadAllThread.start();
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
        InterfaceUpdateService.updateUserInterface(this, UserAction.PRESSED_FILE_SEARCH);
        Thread driveThread = new Thread(() -> {
            try {
                String folderLink;
                if (googleDriveLinkTextBox == null) {
                    folderLink = "";
                } else {
                    folderLink = googleDriveLinkTextBox.getText();
                }
                googleDriveFiles = GoogleDriveService.listFilesFromFolder(folderLink);

                List<String> fileNames = googleDriveFiles.stream()
                        .map(com.google.api.services.drive.model.File::getName)
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    if (cloudFileList != null) {
                        cloudFileList.getItems().setAll(fileNames);
                    }
                });
                if (googleDriveFiles.isEmpty()) {
                    InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
                } else {
                    InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
                }
            } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
                InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
                throw new LauncherErrorException(e.getMessage(), "get files", this);
            }
        });
        driveThread.setDaemon(true);
        driveThread.start();
    }

    @FXML
    public void onDownloadSelectedButtonClick() {
        int selectedIndex = cloudFileList.getSelectionModel().getSelectedIndex();
        if (!HelperMethods.canSelectedFileBeDownloaded(selectedIndex, googleDriveFiles)) {
            return;
        }

        File selectedFile = googleDriveFiles.get(selectedIndex);
        String fileName = selectedFile.getName();
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        InterfaceUpdateService.updateUserInterface(this, UserAction.PRESSED_DOWNLOAD_SINGLE);
        DownloadProgressService progressView =
                new DownloadProgressService(downloadProgressBar, downloadStatusLabel, downloadSelectedButton);
        progressView.begin();

        Runnable downloadTask = () -> {
            try {
                Path destination = Paths.get("downloads", sanitizeFileName(fileName));
                GoogleDriveService.downloadFileWithProgress(selectedFile.getId(), destination,
                        progressView::update);
                progressView.complete();
                InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
            } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
                progressView.failed();
                InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
                throw new LauncherErrorException(e.getMessage(), "download", this);
            }
        };
        Thread downloadThread = new Thread(downloadTask, "google-drive-download");
        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

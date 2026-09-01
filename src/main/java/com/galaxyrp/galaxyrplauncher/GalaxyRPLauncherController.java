package com.galaxyrp.galaxyrplauncher;

import com.galaxyrp.galaxyrplauncher.exceptions.LauncherErrorException;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import com.galaxyrp.galaxyrplauncher.services.AsyncActionService;
import com.galaxyrp.galaxyrplauncher.services.DownloadProgressService;
import com.galaxyrp.galaxyrplauncher.services.GoogleDriveService;
import com.galaxyrp.galaxyrplauncher.services.InterfaceUpdateService;
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
    private AsyncActionService asyncActionService;
    private DownloadProgressService activeProgressView;
    private File activeSelectedFile;
    private String activeFileName;
    private String activeActionName;

    @FXML
    public void initialize() {
        asyncActionService = new AsyncActionService(this);
        InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
    }

    @FXML
    public void onDownloadAllButtonClick() {
        System.out.println("Download All Button clicked");

        if (asyncActionService.isActionRunning()) {
            return;
        }
        if (googleDriveFiles == null || googleDriveFiles.isEmpty()) {
            InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
            return;
        }

        activeProgressView =
                new DownloadProgressService(downloadProgressBar, downloadStatusLabel, downloadAllButton);
        activeActionName = "download all";
        asyncActionService.run(
                UserAction.PRESSED_DOWNLOAD_ALL,
                this::downloadAllFiles,
                this::downloadCompleted,
                this::downloadFailed);
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
        asyncActionService.run(
                UserAction.PRESSED_FILE_SEARCH,
                this::searchForFiles,
                this::searchCompleted,
                this::searchFailed);
    }

    @FXML
    public void onDownloadSelectedButtonClick() {
        if (asyncActionService.isActionRunning()) {
            return;
        }
        int selectedIndex = cloudFileList.getSelectionModel().getSelectedIndex();
        if (!HelperMethods.canSelectedFileBeDownloaded(selectedIndex, googleDriveFiles)) {
            return;
        }

        File selectedFile = googleDriveFiles.get(selectedIndex);
        String fileName = selectedFile.getName();
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        activeSelectedFile = selectedFile;
        activeFileName = fileName;
        activeProgressView =
                new DownloadProgressService(downloadProgressBar, downloadStatusLabel, downloadSelectedButton);
        activeActionName = "download";
        asyncActionService.run(
                UserAction.PRESSED_DOWNLOAD_SINGLE,
                this::downloadSelectedFile,
                this::downloadCompleted,
                this::downloadFailed);
    }

    private void downloadAllFiles() throws IOException, GeneralSecurityException {
        activeProgressView.begin();
        GoogleDriveService.downloadFilesWithProgress(
                googleDriveFiles, Paths.get("downloads"), activeProgressView::update);
    }

    private void downloadSelectedFile() throws IOException, GeneralSecurityException {
        activeProgressView.begin();
        Path destination = Paths.get("downloads", sanitizeFileName(activeFileName));
        GoogleDriveService.downloadFileWithProgress(
                activeSelectedFile.getId(), destination, activeProgressView::update);
    }

    private void downloadCompleted() {
        activeProgressView.complete();
        InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
        clearActiveDownload();
    }

    private void downloadFailed(Exception exception) {
        activeProgressView.failed();
        InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
        new LauncherErrorException(exception.getMessage(), activeActionName, this);
        clearActiveDownload();
    }

    private void searchForFiles() throws IOException, GeneralSecurityException {
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
        Platform.runLater(() -> updateCloudFileList(fileNames));
    }

    private void updateCloudFileList(List<String> fileNames) {
        if (cloudFileList != null) {
            cloudFileList.getItems().setAll(fileNames);
        }
    }

    private void searchCompleted() {
        if (googleDriveFiles.isEmpty()) {
            InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
        } else {
            InterfaceUpdateService.updateUserInterface(this, UserAction.IDLE);
        }
    }

    private void searchFailed(Exception exception) {
        InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
        new LauncherErrorException(exception.getMessage(), "get files", this);
    }

    private void clearActiveDownload() {
        activeProgressView = null;
        activeSelectedFile = null;
        activeFileName = null;
        activeActionName = null;
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

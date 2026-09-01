package com.galaxyrp.galaxyrplauncher;

import com.google.api.services.drive.model.File;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
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
        Thread downloadThread = new Thread(() -> {
            try {
                if(cloudFileList.getSelectionModel().getSelectedIndex() != -1) {
                    int index = cloudFileList.getSelectionModel().getSelectedIndex();
                    File selectedFile = googleDriveFiles.get(index);
                    DriveQuickstart.downloadFile(selectedFile.getId(), Paths.get(""));
                }
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
        downloadThread.setDaemon(true);
        downloadThread.start();
    }
}

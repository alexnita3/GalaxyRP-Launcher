package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.adapters.GoogleDriveAdapter;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import com.galaxyrp.galaxyrplauncher.exceptions.LauncherErrorException;
import com.google.api.services.drive.model.File;
import javafx.application.Platform;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

public class SearchService {
    private final GalaxyRPLauncherController controller;

    public SearchService(GalaxyRPLauncherController controller) {
        this.controller = controller;
    }

    public void search() throws IOException, GeneralSecurityException {
        String folderLink = "";
        if (controller.googleDriveLinkTextBox != null) {
            folderLink = controller.googleDriveLinkTextBox.getText();
        }

        controller.googleDriveFiles = GoogleDriveAdapter.listFilesFromFolder(folderLink);
        List<String> fileNames = controller.googleDriveFiles.stream()
                .map(File::getName)
                .collect(Collectors.toList());
        Platform.runLater(() -> updateFileList(fileNames));
    }

    private void updateFileList(List<String> fileNames) {
        if (controller.cloudFileList != null) {
            controller.cloudFileList.getItems().setAll(fileNames);
        }
    }

    public void complete() {
        if (controller.googleDriveFiles.isEmpty()) {
            InterfaceUpdateService.updateUserInterface(controller, UserAction.NOTHING_TO_DOWNLOAD);
        } else {
            InterfaceUpdateService.updateUserInterface(controller, UserAction.IDLE);
        }
    }

    public void fail(Exception exception) {
        InterfaceUpdateService.updateUserInterface(controller, UserAction.NOTHING_TO_DOWNLOAD);
        new LauncherErrorException(exception.getMessage(), "get files", controller);
    }
}

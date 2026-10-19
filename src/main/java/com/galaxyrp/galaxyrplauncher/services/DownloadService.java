package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.LauncherConfiguration;
import com.galaxyrp.galaxyrplauncher.adapters.GoogleDriveAdapter;
import com.galaxyrp.galaxyrplauncher.exceptions.LauncherErrorException;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import com.google.api.services.drive.model.File;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DownloadService {
    private final GalaxyRPLauncherController controller;
    private final FileComparisonService fileComparisonService;
    private DownloadProgressService progressView;
    private File selectedFile;

    private static final String PK3_PATH = "base";

    public DownloadService(GalaxyRPLauncherController controller) {
        this.controller = controller;
        fileComparisonService = new FileComparisonService();
    }

    public void prepareAll() {
        progressView = new DownloadProgressService(
                controller.downloadProgressBar,
                controller.downloadStatusLabel);
    }

    public void prepareSelected(File file) {
        selectedFile = file;
        progressView = new DownloadProgressService(
                controller.downloadProgressBar,
                controller.downloadStatusLabel);
    }

    public void downloadAll() throws IOException, GeneralSecurityException {
        progressView.begin();
        GoogleDriveAdapter.downloadFilesWithProgress(
                controller.googleDriveFiles,
                file -> getDestinationPath(file, controller.launcherConfiguration),
                progressView::update,
                this::selectFileForDownload,
                file -> filterDownloadedFiles());
    }

    public void downloadSelected() throws IOException, GeneralSecurityException {
        progressView.begin();
        Path destination = getDestinationPath(
                selectedFile, controller.launcherConfiguration);
        GoogleDriveAdapter.downloadFileWithProgress(
                selectedFile.getId(), destination, progressView::update);
        filterDownloadedFiles();
    }

    public void complete() {
        progressView.complete();
        InterfaceUpdateService.updateUserInterface(controller, UserAction.IDLE);
        clear();
    }

    public void fail(Exception exception, String action) {
        progressView.failed();
        InterfaceUpdateService.updateUserInterface(controller, UserAction.IDLE);
        new LauncherErrorException(exception.getMessage(), action, controller);
        clear();
    }

    private void clear() {
        progressView = null;
        selectedFile = null;
    }

    private void filterDownloadedFiles() throws IOException {
        controller.googleDriveFiles =
                fileComparisonService.filterFiles(
                        controller.googleDriveFiles, controller.launcherConfiguration);
        List<String> fileNames = controller.googleDriveFiles.stream()
                .map(File::getName)
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            if (controller.cloudFileList != null) {
                controller.cloudFileList.getItems().setAll(fileNames);
            }
        });
    }

    private void selectFileForDownload(File file) {
        Platform.runLater(() -> {
            for (int index = 0; index < controller.googleDriveFiles.size(); index++) {
                File listedFile = controller.googleDriveFiles.get(index);
                if (file.getId().equals(listedFile.getId())) {
                    controller.cloudFileList.getSelectionModel().select(index);
                    controller.cloudFileList.scrollTo(index);
                    return;
                }
            }
        });
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    static Path getDestinationPath(File file, LauncherConfiguration configuration) {
        String destinationFolder = PK3_PATH;
        if (configuration != null
                && configuration.isSplitDownloads()
                && GoogleDriveAdapter.isInsideMapFolder(file)
                && file.getName() != null
                && file.getName().toLowerCase(Locale.ROOT).endsWith(".pk3")
                && configuration.getGameMod() != null) {
            String modFolder = ModCheckService.getFolderNameFromGameMod(
                    configuration.getGameMod());
            if (modFolder != null) {
                destinationFolder = modFolder;
            }
        }

        return Paths.get(destinationFolder, sanitizeFileName(file.getName()));
    }
}

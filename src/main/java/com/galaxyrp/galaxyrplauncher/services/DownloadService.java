package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.adapters.GoogleDriveAdapter;
import com.galaxyrp.galaxyrplauncher.exceptions.LauncherErrorException;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public class DownloadService {
    private final GalaxyRPLauncherController controller;
    private DownloadProgressService progressView;
    private File selectedFile;

    public DownloadService(GalaxyRPLauncherController controller) {
        this.controller = controller;
    }

    public void prepareAll() {
        progressView = new DownloadProgressService(
                controller.downloadProgressBar,
                controller.downloadStatusLabel,
                controller.downloadAllButton);
    }

    public void prepareSelected(File file) {
        selectedFile = file;
        progressView = new DownloadProgressService(
                controller.downloadProgressBar,
                controller.downloadStatusLabel,
                controller.downloadSelectedButton);
    }

    public void downloadAll() throws IOException, GeneralSecurityException {
        progressView.begin();
        GoogleDriveAdapter.downloadFilesWithProgress(
                controller.googleDriveFiles, Paths.get("downloads"), progressView::update);
    }

    public void downloadSelected() throws IOException, GeneralSecurityException {
        progressView.begin();
        Path destination = Paths.get(
                "downloads", sanitizeFileName(selectedFile.getName()));
        GoogleDriveAdapter.downloadFileWithProgress(
                selectedFile.getId(), destination, progressView::update);
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

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

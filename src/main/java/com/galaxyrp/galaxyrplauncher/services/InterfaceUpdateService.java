package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.LauncherConfiguration;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import com.google.api.services.drive.model.File;
import javafx.application.Platform;

public class InterfaceUpdateService {
    public static void updateUserInterface(GalaxyRPLauncherController controller, UserAction userAction) {
        Runnable update = () -> {
            switch (userAction) {
                case PRESSED_DOWNLOAD_SINGLE, PRESSED_DOWNLOAD_ALL:
                    changeButtonState(controller, true);
                    setStatus(controller, "Awaiting download...");
                    break;
                case PRESSED_FILE_SEARCH:
                    changeButtonState(controller, true);
                    setStatus(controller, "Searching...");
                    break;
                case PRESSED_LAUNCH_GAME:
                    changeButtonState(controller, true);
                    setStatus(controller, "Launching...");
                    break;
                case NOTHING_TO_DOWNLOAD:
                    setEmptyState(controller);
                    break;
                case IDLE:
                    setIdleState(controller);
                    break;
            }
        };

        if (Platform.isFxApplicationThread()) {
            update.run();
        } else {
            Platform.runLater(update);
        }
    }

    private static void changeButtonState(GalaxyRPLauncherController controller, boolean greyedOut) {
        controller.checkUpdateButton.setDisable(greyedOut);
        controller.launchGameButton.setDisable(greyedOut);
        controller.downloadSelectedButton.setDisable(greyedOut);
        controller.downloadAllButton.setDisable(greyedOut);
        controller.cloudFileList.setDisable(greyedOut);
    }

    private static void setEmptyState(GalaxyRPLauncherController controller) {
        controller.checkUpdateButton.setDisable(false);
        controller.downloadSelectedButton.setDisable(true);
        controller.downloadAllButton.setDisable(true);
        controller.cloudFileList.setDisable(true);
        setStatus(controller, "Search for updates or start the game.");
    }

    private static void setIdleState(GalaxyRPLauncherController controller) {
        boolean hasFiles = controller.googleDriveFiles != null
                && !controller.googleDriveFiles.isEmpty();

        if (hasFiles) {
            changeButtonState(controller, false);
            setStatus(controller, "Ready.");
        } else {
            setEmptyState(controller);
        }
    }

    private static void setStatus(GalaxyRPLauncherController controller, String status) {
        if (controller.statusLabel != null) {
            controller.statusLabel.setText(status);
        }
    }

    public static void updateFileDetailLabels(GalaxyRPLauncherController controller, File file) {
        controller.fileNameLabel.setText(file.getName());
        controller.fileSizeLabel.setText(file.getSize() / 1000000 + " MB"); //We want to display MB
        controller.fileVersionNumberLabel.setText(String.valueOf(file.getVersion()));
        controller.fileLastChangedLabel.setText(String.valueOf(file.getModifiedTime()));
    }

    public static void displayConfigurationValues(GalaxyRPLauncherController controller, LauncherConfiguration configuration) {
        controller.server1IpTextBox.setText(configuration.getServerIp());
        controller.server2IpTextBox.setText(configuration.getServer2Ip());
        controller.server1NameTextBox.setText(configuration.getServerName());
        controller.server2NameTextBox.setText(configuration.getServer2Name());
        controller.googleDriveLinkTextBox.setText(configuration.getGoogleDriveLink());
        controller.clientModDropDown.setValue(configuration.getClientMod());
        controller.resolutionXTextBox.setText(String.valueOf(configuration.getResolutionX()));
        controller.resolutionYTextBox.setText(String.valueOf(configuration.getResolutionY()));
        controller.customArgumentsTextBox.setText(configuration.getCustomArguments());
        controller.scanOnStartCheckBox.setSelected(configuration.isScanOnStartup());
        controller.automaticallyDownloadCheckBox.setSelected(configuration.isAutoDownload());

        controller.serverSelectDropDownList.getItems().clear();
        controller.serverSelectDropDownList.getItems().addAll(configuration.getServerIp(), configuration.getServer2Ip());
    }

}

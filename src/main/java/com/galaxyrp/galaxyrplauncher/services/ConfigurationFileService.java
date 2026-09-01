package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.LauncherConfiguration;
import com.galaxyrp.galaxyrplauncher.enums.GameMods;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConfigurationFileService {
    private static final String CONFIGURATION_FILE_NAME = "GalaxyRP_Launcher_config.config";
    private static final Gson JSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private boolean doesConfigurationFileExist() {
        return Files.isRegularFile(getConfigurationFilePath());
    }

    public void initializeConfigurationFile() throws IOException {
        if (!doesConfigurationFileExist()) {
            LauncherConfiguration launcherConfiguration = new LauncherConfiguration();
            saveConfigurationFile(launcherConfiguration);
        }
    }

    public void saveConfigurationFile(LauncherConfiguration configuration) throws IOException {
        String configurationJson = JSON.toJson(configuration);

        Files.writeString(
                getConfigurationFilePath(),
                configurationJson,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    public LauncherConfiguration loadConfigurationFile() throws IOException {
        LauncherConfiguration launcherConfiguration = new LauncherConfiguration();
        if (doesConfigurationFileExist()) {
            String configurationJson = Files.readString(
                    getConfigurationFilePath(), StandardCharsets.UTF_8);
            if (!configurationJson.isBlank()) {
                launcherConfiguration = JSON.fromJson(
                        configurationJson, LauncherConfiguration.class);
            }
        }
        return launcherConfiguration;
    }

    public void applyConfiguration(GalaxyRPLauncherController controller) throws IOException {
        LauncherConfiguration configuration = controller.launcherConfiguration;
        if (configuration == null) {
            configuration = new LauncherConfiguration();
            controller.launcherConfiguration = configuration;
        }

        configuration.setServerIp(controller.server1IpTextBox.getText());
        configuration.setServerName(controller.server1NameTextBox.getText());
        configuration.setServer2Ip(controller.server2IpTextBox.getText());
        configuration.setServer2Name(controller.server2NameTextBox.getText());
        configuration.setGoogleDriveLink(controller.googleDriveLinkTextBox.getText());
        configuration.setClientMod((GameMods) controller.clientModDropDown.getValue());
        configuration.setResolutionX(parseResolution(controller.resolutionXTextBox.getText(), "X"));
        configuration.setResolutionY(parseResolution(controller.resolutionYTextBox.getText(), "Y"));
        configuration.setCustomArguments(controller.customArgumentsTextBox.getText());
        configuration.setScanOnStartup(controller.scanOnStartCheckBox.isSelected());
        configuration.setAutoDownload(controller.automaticallyDownloadCheckBox.isSelected());

        controller.serverSelectDropDownList.getItems().clear();
        controller.serverSelectDropDownList.getItems().addAll(configuration.getServerIp(), configuration.getServer2Ip());

        saveConfigurationFile(configuration);
    }

    private static int parseResolution(String value, String axis) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Resolution " + axis + " must contain an integer.");
        }
        return Integer.parseInt(value.trim());
    }

    private static Path getConfigurationFilePath() {
        return Paths.get(System.getProperty("user.dir"))
                .toAbsolutePath()
                .resolve(CONFIGURATION_FILE_NAME);
    }
}

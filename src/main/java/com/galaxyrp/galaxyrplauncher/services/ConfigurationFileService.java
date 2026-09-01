package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.LauncherConfiguration;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConfigurationFileService {
    private static final String CONFIGURATION_FILE_NAME = "GalaxyRP_Launcher_config.config";

    private boolean doesConfigurationFileExist() {
        return Files.isRegularFile(Path.of(CONFIGURATION_FILE_NAME));
    }

    private void initializeNewConfigurationFile() throws IOException {
        if (!doesConfigurationFileExist()) {
            LauncherConfiguration launcherConfiguration = new LauncherConfiguration();
            saveConfigurationFile(launcherConfiguration);
        }
    }

    public void saveConfigurationFile(LauncherConfiguration configuration) throws IOException {
        if (doesConfigurationFileExist()) {

            String configurationJson =
                    GsonFactory.getDefaultInstance().toString(configuration);

            Files.writeString(
                    Path.of(CONFIGURATION_FILE_NAME),
                    configurationJson,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW);
        }
    }

    public LauncherConfiguration loadConfigurationFile() throws IOException {
        LauncherConfiguration launcherConfiguration = new LauncherConfiguration();
        if (doesConfigurationFileExist()) {
            String configurationJson = Files.readString(Path.of(CONFIGURATION_FILE_NAME), StandardCharsets.UTF_8);
            launcherConfiguration = GsonFactory.getDefaultInstance().fromString(configurationJson, LauncherConfiguration.class);
        }
        return launcherConfiguration;
    }

    public void applyConfiguration(LauncherConfiguration configuration) {

    }
}

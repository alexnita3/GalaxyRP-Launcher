package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.LauncherConfiguration;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConfigurationFileService {
    private static final String CONFIGURATION_FILE_NAME = "GalaxyRP_Launcher_config.config";

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
        String configurationJson =
                GsonFactory.getDefaultInstance().toString(configuration);

        Files.writeString(
                getConfigurationFilePath(),
                configurationJson,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public LauncherConfiguration loadConfigurationFile() throws IOException {
        LauncherConfiguration launcherConfiguration = new LauncherConfiguration();
        if (doesConfigurationFileExist()) {
            String configurationJson = Files.readString(
                    getConfigurationFilePath(), StandardCharsets.UTF_8);
            launcherConfiguration = GsonFactory.getDefaultInstance().fromString(configurationJson, LauncherConfiguration.class);
        }
        return launcherConfiguration;
    }

    public void applyConfiguration(LauncherConfiguration configuration) {

    }

    private static Path getConfigurationFilePath() {
        return Paths.get(System.getProperty("user.dir"))
                .toAbsolutePath()
                .resolve(CONFIGURATION_FILE_NAME);
    }
}

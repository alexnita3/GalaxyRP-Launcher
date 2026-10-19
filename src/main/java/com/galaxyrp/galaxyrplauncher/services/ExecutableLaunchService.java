package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.LauncherConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExecutableLaunchService {
    public static void launchGame(LauncherConfiguration launcherConfiguration, GalaxyRPLauncherController galaxyRPLauncherController) {
        Path gameDirectory = Path.of("").toAbsolutePath();
        String executable = ModCheckService.getAvailableClientModExecutables()
                .get(launcherConfiguration.getClientMod());
        if (executable == null) {
            galaxyRPLauncherController.statusLabel.setText("The selected client executable was not found in the game folder.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add(gameDirectory.resolve(executable).toString());

        String arguments = buildFullExecutableArguments(launcherConfiguration, galaxyRPLauncherController);

        if (!arguments.isBlank()) {
            command.addAll(List.of(arguments.trim().split("\\s+")));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(gameDirectory.toFile());
        try {
            processBuilder.start();
        } catch (IOException e) {
            galaxyRPLauncherController.statusLabel.setText("Unable to launch game: " + e.getMessage());
        }
    }

    public static String buildFullExecutableArguments(LauncherConfiguration launcherConfiguration, GalaxyRPLauncherController galaxyRPLauncherController){
        List<String> arguments = new ArrayList<>();

        Object selectedServer = galaxyRPLauncherController.serverSelectDropDownList.getValue();
        if (selectedServer != null && !selectedServer.toString().isBlank()) {
            arguments.add("+connect");
            arguments.add(selectedServer.toString().trim());
        }

        if (launcherConfiguration != null && launcherConfiguration.getGameMod() != null) {
            String gameModArguments =
                    ModCheckService.getGameStartArgumentForGameMod(launcherConfiguration.getGameMod());
            if (gameModArguments != null && !gameModArguments.isBlank()) {
                arguments.add(gameModArguments);
            }
        }

        if (launcherConfiguration != null) {
            String customArguments = launcherConfiguration.getCustomArguments();
            if (customArguments != null && !customArguments.isBlank()) {
                arguments.add(customArguments.trim());
            }
        }

        return String.join(" ", arguments);
    }
}

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

        String serverConnect = "+connect ";
        serverConnect += galaxyRPLauncherController.serverSelectDropDownList.getValue().toString();

        String gameMod = "+set fs_game ";
        gameMod += ModCheckService.getGameStartArgumentForGameMod(launcherConfiguration.getGameMod());

        String customArguments = launcherConfiguration.getCustomArguments();

        String fullArguments = serverConnect + " " + gameMod + " " + customArguments;
        return fullArguments;
    }
}

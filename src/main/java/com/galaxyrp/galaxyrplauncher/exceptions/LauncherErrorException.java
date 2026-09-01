package com.galaxyrp.galaxyrplauncher.exceptions;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;

public class LauncherErrorException extends RuntimeException {
    public LauncherErrorException(String message, String action, GalaxyRPLauncherController controller) {
        String statusText = "Something went wrong when trying to " + action + ". The error was: " + message;
        controller.statusLabel.setText(statusText);
    }
}

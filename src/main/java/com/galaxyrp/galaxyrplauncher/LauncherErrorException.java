package com.galaxyrp.galaxyrplauncher;

public class LauncherErrorException extends RuntimeException {
    public LauncherErrorException(String message, String action, GalaxyRPLauncherController controller) {
        String statusText = "Something went wrong when trying to " + action + ". The error was: " + message;
        controller.statusLabel.setText(statusText);
    }
}

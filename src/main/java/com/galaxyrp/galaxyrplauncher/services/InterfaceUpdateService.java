package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.GalaxyRPLauncherController;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;

public class InterfaceUpdateService {
    public static void updateUserInterface(GalaxyRPLauncherController controller, UserAction userAction) {
     switch(userAction) {
         case PRESSED_DOWNLOAD_SINGLE, PRESSED_DOWNLOAD_ALL:
             changeButtonState(controller, true);
             controller.statusLabel.setText("Awaiting download...");
             break;
         case PRESSED_FILE_SEARCH:
             changeButtonState(controller, true);
             controller.statusLabel.setText("Searching...");
             break;
         case PRESSED_LAUNCH_GAME:
             changeButtonState(controller, true);
             controller.statusLabel.setText("Launching...");
             break;
         default:
             changeButtonState(controller, false);
             controller.statusLabel.setText("Ready.");
             break;

     }
    }

    private static void changeButtonState(GalaxyRPLauncherController controller, boolean greyedOut) {
        controller.checkUpdateButton.setDisable(greyedOut);
        controller.launchGameButton.setDisable(greyedOut);
        controller.downloadSelectedButton.setDisable(greyedOut);
        controller.downloadAllButton.setDisable(greyedOut);
        controller.cloudFileList.setDisable(greyedOut);
    }


}

package com.galaxyrp.galaxyrplauncher;

import com.google.api.services.drive.model.File;

import java.util.List;

public class HelperMethods {


    public static void updateFileDetailLabels(GalaxyRPLauncherController controller, File file) {
        controller.fileNameLabel.setText(file.getName());
        controller.fileSizeLabel.setText(file.getSize() / 1000000 + " MB"); //We want to display MB
        controller.fileVersionNumberLabel.setText(String.valueOf(file.getVersion()));
        controller.fileLastChangedLabel.setText(String.valueOf(file.getModifiedTime()));
    }

    public static boolean canSelectedFileBeDownloaded(int selectedIndex, List<File> driveFiles) {

        if(driveFiles.isEmpty() || selectedIndex == -1) {
            return false;
        }

        if(selectedIndex >= driveFiles.size() || selectedIndex < 0) {
            return false;
        }

        return true;
    }

}

package com.galaxyrp.galaxyrplauncher;

import com.google.api.services.drive.model.File;

public class HelperMethods {


    public static void updateFileDetailLabels(GalaxyRPLauncherController controller, File file) {
        controller.fileNameLabel.setText(file.getName());
        controller.fileSizeLabel.setText(String.valueOf(file.getSize()));
        controller.fileVersionNumberLabel.setText(String.valueOf(file.getVersion()));
        controller.fileLastChangedLabel.setText(String.valueOf(file.getModifiedTime()));
    }

}

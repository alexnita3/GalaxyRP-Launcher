package com.galaxyrp.galaxyrplauncher;

import com.galaxyrp.galaxyrplauncher.enums.GameMods;
import com.google.api.services.drive.model.File;

import java.util.List;

public class HelperMethods {

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

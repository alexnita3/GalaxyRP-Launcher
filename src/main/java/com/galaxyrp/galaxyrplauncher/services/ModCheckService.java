package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.enums.GameMods;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModCheckService {

    public List<GameMods> getAvailableGameMods() {
        Path rootDirectory = Path.of("").toAbsolutePath();
        List<GameMods> availableGameMods = new ArrayList<>();

        for (GameMods gameMod : GameMods.values()) {
            String folderName = getFolderNameFromGameMod(gameMod);
            if (folderName != null && Files.isDirectory(rootDirectory.resolve(folderName))) {
                availableGameMods.add(gameMod);
            }
        }

        return availableGameMods;
    }

    private static String getFolderNameFromGameMod(GameMods gameMod) {
        switch(gameMod) {
            case BASE_JKA:
                return "base";
            case GALAXY_RP:
                return "GalaxyRP";
            case OPEN_JK:
                return "OpenJk";
            default:
                return null;
        }
    }

}

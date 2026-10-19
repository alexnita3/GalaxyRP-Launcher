package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.enums.ClientMods;
import com.galaxyrp.galaxyrplauncher.enums.GameMods;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public List<ClientMods> getAvailableClientMods() {
        List<ClientMods> availableClientMods = new ArrayList<>();

        List<String> allExecutables = getAllExecutableFiles();

        for (ClientMods clientMod : ClientMods.values()) {
            String executableName = getExecutableNameFromClientMod(clientMod);
            for(String executable : allExecutables) {
                if(executable.startsWith(executableName)) {
                    availableClientMods.add(clientMod);
                }
            }
        }

        return availableClientMods;
    }

    public List<String> getAllExecutableFiles(){
        List<String> executableFiles = new ArrayList<>();

        Path rootDirectory = Path.of("").toAbsolutePath();
        Path dirPath = Paths.get(rootDirectory.toUri());
        File[] files = dirPath.toFile().listFiles();
        if (files == null) {
            return null;
        }

        for (File f : files) {
            if (Files.isExecutable(f.toPath()) && f.canExecute()) {
                //TODO: Make this compatible with Linux executables
                //Exclude dedicated servers
                if(!f.getName().contains("ded") && f.getName().endsWith(".exe")) {
                    executableFiles.add(f.getName());
                }
            }
        }

        if(executableFiles.isEmpty()){
            return null;
        }

        return executableFiles;
    }

    private static String getFolderNameFromGameMod(GameMods gameMod) {
        switch(gameMod) {
            case BASE_JKA:
                return "base";
            case GALAXY_RP:
                return "GalaxyRP";
            case OPEN_JK:
                return "OpenJk";
            case TAYST_JK:
                return "taystjk";
            case JA_PRO:
                return "JaPro";
            default:
                return null;
        }
    }

    private static String getGameStartArgumentForGameMod(GameMods gameMod) {
        String argument = switch (gameMod) {
            case GALAXY_RP -> "GalaxyRP";
            case OPEN_JK -> "OpenJk";
            case TAYST_JK -> "taystjk";
            case JA_PRO -> "JaPro";
            default -> "";
        };

        if(argument.isEmpty()) {
            return null;
        } else {
            return "+set fs_game " + argument;
        }
    }

    //TODO: refactor this to search for the file names, that way it deals gracefully with the different file names per 32/64 bit
    //this only work on windows for now, also
    public static String getExecutableNameFromClientMod(ClientMods clientMod) {
        return switch (clientMod) {
            case BASE_JKA -> "jamp";
            case OPEN_JK -> "openjk";
            case TAYST_JK -> "taystjk";
            default -> "jamp";
        };
    }

}

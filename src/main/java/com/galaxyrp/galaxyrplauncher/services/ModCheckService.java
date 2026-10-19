package com.galaxyrp.galaxyrplauncher.services;

import com.galaxyrp.galaxyrplauncher.enums.ClientMods;
import com.galaxyrp.galaxyrplauncher.enums.GameMods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public static Map<ClientMods, String> getAvailableClientModExecutables() {
        List<String> allExecutables = getAllExecutableFiles();
        Map<ClientMods, String> availableExecutables = new EnumMap<>(ClientMods.class);

        if (allExecutables != null) {
            for (ClientMods clientMod : ClientMods.values()) {
                String executableName = getExecutableNameFromClientMod(clientMod);
                for (String executable : allExecutables) {
                    if (executable.startsWith(executableName)) {
                        availableExecutables.put(clientMod, executable);
                        break;
                    }
                }
            }
        }

        return availableExecutables;
    }

    public static List<String> getAllExecutableFiles(){
        List<String> executableFiles = new ArrayList<>();

        Path rootDirectory = Path.of("").toAbsolutePath();
        File[] files = rootDirectory.toFile().listFiles();
        if (files == null) {
            return null;
        }

        for (File f : files) {
            Path executablePath = f.toPath();
            String executableName = f.getName().toLowerCase(Locale.ROOT);
            if (Files.isRegularFile(executablePath)
                    && f.canExecute()
                    && isNativeExecutable(executablePath, executableName)
                    && !executableName.contains("ded")) {
                executableFiles.add(f.getName());
            }
        }

        if(executableFiles.isEmpty()){
            return null;
        }

        return executableFiles;
    }

    private static boolean isNativeExecutable(Path path, String fileName) {
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        try (InputStream inputStream = Files.newInputStream(path)) {
            byte[] signature = inputStream.readNBytes(4);
            if (operatingSystem.contains("win")) {
                return fileName.endsWith(".exe")
                        && signature.length >= 2
                        && signature[0] == 'M'
                        && signature[1] == 'Z';
            }

            return Files.isExecutable(path)
                    && signature.length == 4
                    && (signature[0] & 0xff) == 0x7f
                    && signature[1] == 'E'
                    && signature[2] == 'L'
                    && signature[3] == 'F';
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to inspect executable candidate: " + path, exception);
        }
    }

    public static String getFolderNameFromGameMod(GameMods gameMod) {
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

    static String getGameStartArgumentForGameMod(GameMods gameMod) {
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

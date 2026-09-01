package com.galaxyrp.galaxyrplauncher.services;

import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileComparisonService {
    private static final Path DOWNLOADS_DIRECTORY = Paths.get("downloads");

    public List<File> filterFiles(List<File> googleDriveFiles) throws IOException {
        googleDriveFiles = filterPk3Files(googleDriveFiles);
        googleDriveFiles = getDifferentFiles(googleDriveFiles);
        return googleDriveFiles;
    }

    private List<File> filterPk3Files(List<File> files) {
        if (files == null) {
            throw new IllegalArgumentException("File list cannot be null.");
        }

        List<File> pk3Files = new ArrayList<>();
        for (File file : files) {
            if (file == null || file.getName() == null) {
                continue;
            }

            if (file.getName().toLowerCase(Locale.ROOT).endsWith(".pk3")) {
                pk3Files.add(file);
            }
        }
        return pk3Files;
    }

    private List<File> getDifferentFiles(List<File> googleDriveFiles)
            throws IOException {
        if (googleDriveFiles == null) {
            throw new IllegalArgumentException("Google Drive file list cannot be null.");
        }

        List<File> differentFiles = new ArrayList<>();
        for (File googleDriveFile : googleDriveFiles) {
            if (googleDriveFile == null || googleDriveFile.getName() == null
                    || googleDriveFile.getName().isBlank()) {
                throw new IllegalArgumentException(
                        "Google Drive file list contains a file without a name.");
            }

            Path localFile = DOWNLOADS_DIRECTORY.resolve(
                    sanitizeFileName(googleDriveFile.getName()));
            if (!Files.isRegularFile(localFile)) {
                differentFiles.add(googleDriveFile);
                continue;
            }

            String remoteChecksum = googleDriveFile.getMd5Checksum();
            if (remoteChecksum == null
                    || !remoteChecksum.equalsIgnoreCase(calculateMd5Checksum(localFile))) {
                differentFiles.add(googleDriveFile);
            }
        }

        return differentFiles;
    }

    private static String calculateMd5Checksum(Path file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("MD5 is not available.", exception);
        }

        byte[] buffer = new byte[8192];
        try (InputStream inputStream = Files.newInputStream(file)) {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        StringBuilder checksum = new StringBuilder();
        for (byte value : digest.digest()) {
            checksum.append(String.format("%02x", value & 0xff));
        }
        return checksum.toString();
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

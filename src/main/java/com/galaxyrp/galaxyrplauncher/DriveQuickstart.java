package com.galaxyrp.galaxyrplauncher;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* class to demonstrate use of Drive files list API */
public class DriveQuickstart {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.

        InputStream in = Files.newInputStream(Paths.get(CREDENTIALS_FILE_PATH));

        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    private static String extractFolderId(String folderLink) {
        if (folderLink == null || folderLink.isBlank()) {
            return null;
        }

        String cleaned = folderLink.trim();
        Matcher matcher = Pattern.compile("(?:/folders/|[?&]id=)([A-Za-z0-9_-]+)").matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1);
        }

        if (cleaned.matches("[A-Za-z0-9_-]{10,}")) {
            return cleaned;
        }

        return null;
    }

    private static void collectFilesRecursively(Drive service, String folderId, List<File> allFiles)
            throws IOException {
        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("nextPageToken, files(id, name, mimeType, webViewLink, size, modifiedTime, version)")
                    .setPageSize(1000)
                    .setPageToken(pageToken)
                    .setSupportsAllDrives(true)
                    .execute();

            List<File> children = result.getFiles();
            if (children != null) {
                for (File child : children) {
                    if ("application/vnd.google-apps.folder".equals(child.getMimeType())) {
                        collectFilesRecursively(service, child.getId(), allFiles);
                    } else {
                        allFiles.add(child);
                    }
                }
            }

            pageToken = result.getNextPageToken();
        } while (pageToken != null && !pageToken.isEmpty());
    }

    public static List<File> listFilesFromFolder(String folderLink) throws IOException, GeneralSecurityException {
        String folderId = extractFolderId(folderLink);
        if (folderId == null || folderId.isBlank()) {
            throw new IllegalArgumentException("Invalid Google Drive folder URL or ID: " + folderLink);
        }

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        List<File> allFiles = new java.util.ArrayList<>();
        collectFilesRecursively(service, folderId, allFiles);
        for(File file : allFiles) {
            System.out.println(file.getName());
        }
        return allFiles;
    }

    public static void downloadFile(String fileId, Path destination)
            throws IOException, GeneralSecurityException {
        if (fileId == null || fileId.isBlank()) {
            throw new IllegalArgumentException("Google Drive file ID cannot be blank.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Download destination cannot be null.");
        }

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (OutputStream outputStream = Files.newOutputStream(destination)) {
            service.files()
                    .get(fileId)
                    .setAlt("media")
                    .setSupportsAllDrives(true)
                    .executeMediaAndDownloadTo(outputStream);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 403) {
                throw new IOException(
                        "The signed-in Google account does not have permission to download file "
                                + fileId
                                + ". Share the file with that account, or authorize the correct account.",
                        e);
            }
            throw e;
        }
    }

    public static void listAllFiles(String... args) throws IOException, GeneralSecurityException {
        List<File> files = listFilesFromFolder("");
        if (files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        listAllFiles();
    }


}

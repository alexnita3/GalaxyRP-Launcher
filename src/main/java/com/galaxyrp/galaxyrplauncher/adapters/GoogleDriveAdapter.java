package com.galaxyrp.galaxyrplauncher.adapters;

import com.galaxyrp.galaxyrplauncher.services.FileComparisonService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
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
public class GoogleDriveAdapter {
    private static final int KILOBYTE = 1024;
    private static final int MEGABYTE = KILOBYTE * KILOBYTE;
    private static final int MAX_DOWNLOAD_CHUNK_SIZE = 32 * MEGABYTE;

    @FunctionalInterface
    public interface DownloadProgressListener {
        void onProgress(long downloadedBytes, long totalBytes);
    }

    @FunctionalInterface
    public interface FileDownloadedListener {
        void onFileDownloaded(File file) throws IOException;
    }

    @FunctionalInterface
    public interface FileDownloadStartedListener {
        void onFileDownloadStarted(File file);
    }

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

    private static String resolveProjectRoot() {
        String userDir = System.getProperty("user.dir");
        if (userDir != null && !userDir.isBlank()) {
            return userDir;
        }
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    private static InputStream openCredentialStream() throws IOException {
        List<Path> candidates = new java.util.ArrayList<>();

        String customPath = System.getProperty("google.credentials.file", System.getProperty("credentials.file"));
        if (customPath != null && !customPath.isBlank()) {
            candidates.add(Paths.get(customPath));
        }

        String root = resolveProjectRoot();
        candidates.add(Paths.get(root, "src", "main", "resources", CREDENTIALS_FILE_PATH));
        candidates.add(Paths.get(root, CREDENTIALS_FILE_PATH));

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.newInputStream(candidate);
            }
        }

        InputStream classpathStream = GoogleDriveAdapter.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (classpathStream != null) {
            return classpathStream;
        }

        throw new FileNotFoundException("Could not find Google Drive credentials file. Tried: " + candidates + ", and classpath resource: credentials.json");
    }

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
        try (InputStream in = openCredentialStream()) {
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
                    .setFields("nextPageToken, files(id, name, mimeType, webViewLink, size, md5Checksum, modifiedTime, version)")
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

        FileComparisonService fileComparisonService = new FileComparisonService();

        List<File> filteredFiles = fileComparisonService.filterFiles(allFiles);

        return filteredFiles;
    }

    public static void downloadFilesWithProgress(
            List<File> files,
            Path destinationDirectory,
            DownloadProgressListener progressListener,
            FileDownloadStartedListener fileDownloadStartedListener,
            FileDownloadedListener fileDownloadedListener)
            throws IOException, GeneralSecurityException {
        if (files == null) {
            throw new IllegalArgumentException("File list cannot be null.");
        }
        if (destinationDirectory == null) {
            throw new IllegalArgumentException("Destination directory cannot be null.");
        }
        if (progressListener == null) {
            throw new IllegalArgumentException("Progress listener cannot be null.");
        }
        if (fileDownloadedListener == null) {
            throw new IllegalArgumentException("File downloaded listener cannot be null.");
        }
        if (fileDownloadStartedListener == null) {
            throw new IllegalArgumentException("File download started listener cannot be null.");
        }

        Files.createDirectories(destinationDirectory);

        for (File file : files) {
            if (file == null || file.getId() == null || file.getId().isBlank()) {
                throw new IllegalArgumentException("File list contains a file without an ID.");
            }
            if (file.getName() == null || file.getName().isBlank()) {
                throw new IllegalArgumentException("File list contains a file without a name.");
            }

            Path destination = destinationDirectory.resolve(sanitizeFileName(file.getName()));
            fileDownloadStartedListener.onFileDownloadStarted(file);
            long totalBytes = -1;
            if (file.getSize() != null) {
                totalBytes = file.getSize();
            }
            progressListener.onProgress(0, totalBytes);
            downloadFileWithProgress(file.getId(), destination, progressListener);
            fileDownloadedListener.onFileDownloaded(file);
        }

    }

    public static void downloadFileWithProgress(
            String fileId, Path destination, DownloadProgressListener progressListener)
            throws IOException, GeneralSecurityException {
        if (fileId == null || fileId.isBlank()) {
            throw new IllegalArgumentException("Google Drive file ID cannot be blank.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Download destination cannot be null.");
        }
        if (progressListener == null) {
            throw new IllegalArgumentException("Progress listener cannot be null.");
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
            File metadata = service.files()
                    .get(fileId)
                    .setFields("size, mimeType")
                    .setSupportsAllDrives(true)
                    .execute();
            long totalBytes;
            if (metadata.getSize() == null) {
                totalBytes = -1;
            } else {
                totalBytes = metadata.getSize();
            }

            Drive.Files.Get request = service.files()
                    .get(fileId)
                    .setAlt("media")
                    .setSupportsAllDrives(true);
            request.getMediaHttpDownloader()
                    .setDirectDownloadEnabled(false)
                    .setChunkSize(getChunkSize(totalBytes))
                    .setProgressListener(
                    (MediaHttpDownloaderProgressListener) downloader ->
                            progressListener.onProgress(
                                    downloader.getNumBytesDownloaded(),
                                    totalBytes));
            request.executeMediaAndDownloadTo(outputStream);
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

    private static int getChunkSize(long totalBytes) {
        if (totalBytes <= 0) {
            return 8 * MEGABYTE;
        }
        if (totalBytes <= 4L * MEGABYTE) {
            return (int) Math.max(KILOBYTE, totalBytes);
        }
        if (totalBytes <= 64L * MEGABYTE) {
            return 4 * MEGABYTE;
        }
        if (totalBytes <= 512L * MEGABYTE) {
            return 8 * MEGABYTE;
        }
        if (totalBytes <= 2L * 1024 * MEGABYTE) {
            return 16 * MEGABYTE;
        }
        return MAX_DOWNLOAD_CHUNK_SIZE;
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

}

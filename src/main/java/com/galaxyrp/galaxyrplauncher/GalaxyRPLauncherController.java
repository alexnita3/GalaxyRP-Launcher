package com.galaxyrp.galaxyrplauncher;

import com.galaxyrp.galaxyrplauncher.enums.ClientMods;
import com.galaxyrp.galaxyrplauncher.enums.UserAction;
import com.galaxyrp.galaxyrplauncher.services.*;
import com.google.api.services.drive.model.File;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;

public class GalaxyRPLauncherController {
    public TextField server1IpTextBox;
    public TextField server1NameTextBox;
    public TextField server2IpTextBox;
    public TextField server2NameTextBox;
    public TextField googleDriveLinkTextBox;
    public TextField resolutionXTextBox;
    public TextField customArgumentsTextBox;
    public ChoiceBox clientModDropDown;
    public CheckBox scanOnStartCheckBox;
    public TextField resolutionYTextBox;
    public CheckBox automaticallyDownloadCheckBox;
    public Label fileNameLabel;
    public Label fileSizeLabel;
    public Label fileVersionNumberLabel;
    public Label fileLastChangedLabel;
    public Button downloadSelectedButton;
    public Button checkUpdateButton;
    public Button downloadAllButton;
    public Button launchGameButton;
    public ChoiceBox serverSelectDropDownList;
    public ListView<String> cloudFileList;
    public ProgressBar downloadProgressBar;
    public Label downloadStatusLabel;
    public Label statusLabel;

    public List<File> googleDriveFiles;
    public LauncherConfiguration launcherConfiguration;
    public Button applyConfig;
    public ChoiceBox gameModDropDown;
    public CheckBox scanOnStartCheckBox1;
    public Label playerCount1Label;
    public ListView playerList1;
    public Button refreshServer1Info;
    public Label playerCount2Label;
    public ListView playerList2;
    public Button refreshServer2Info;
    public Label server1InfoTitle;
    public Label server2InfoTitle;

    private AsyncActionService asyncActionService;
    private SearchService searchService;
    private DownloadService downloadService;

    @FXML
    public void initialize() throws IOException {
        asyncActionService = new AsyncActionService(this);
        searchService = new SearchService(this);
        downloadService = new DownloadService(this);
        cloudFileList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> displayFileDetails());
        InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);

        ConfigurationFileService configurationFileService = new ConfigurationFileService();
        configurationFileService.initializeConfigurationFile();
        this.launcherConfiguration = configurationFileService.loadConfigurationFile();
        InterfaceUpdateService.displayConfigurationValues(this, launcherConfiguration);

        clientModDropDown.getItems().add(0, ClientMods.OPEN_JK);
        clientModDropDown.getItems().add(1, ClientMods.BASE_JKA);

        Runnable startupAction = () -> {
            if (launcherConfiguration.isScanOnStartup()) {
                onCheckUpdateButtonClick();
            } else if (launcherConfiguration.isAutoDownload()) {
                onDownloadAllButtonClick();
            }
        };

        if (hasText(launcherConfiguration.getServerIp())
                && hasText(launcherConfiguration.getServer2Ip())) {
            asyncActionService.run(
                    UserAction.UPDATE_SERVER_TRACKER,
                    () -> new InterfaceUpdateService().updateServerTrackerLists(this),
                    startupAction,
                    exception -> {
                        exception.printStackTrace();
                        startupAction.run();
                    });
        } else {
            startupAction.run();
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @FXML
    public void onDownloadAllButtonClick() {
        System.out.println("Download All Button clicked");

        if (asyncActionService.isActionRunning()) {
            return;
        }
        if (googleDriveFiles == null || googleDriveFiles.isEmpty()) {
            InterfaceUpdateService.updateUserInterface(this, UserAction.NOTHING_TO_DOWNLOAD);
            return;
        }

        downloadService.prepareAll();
        asyncActionService.run(
                UserAction.PRESSED_DOWNLOAD_ALL,
                downloadService::downloadAll,
                downloadService::complete,
                exception -> downloadService.fail(exception, "download all"));
    }

    @FXML
    public void displayFileDetails() {
        if (cloudFileList.getSelectionModel().getSelectedIndex() != -1) {
            int index = cloudFileList.getSelectionModel().getSelectedIndex();

            File selectedFile = googleDriveFiles.get(index);

            InterfaceUpdateService.updateFileDetailLabels(this, selectedFile);
        }
    }

    @FXML
    public void onCheckUpdateButtonClick() {
        asyncActionService.run(
                UserAction.PRESSED_FILE_SEARCH,
                searchService::search,
                searchService::complete,
                searchService::fail);
    }

    @FXML
    public void onDownloadSelectedButtonClick() {
        if (asyncActionService.isActionRunning()) {
            return;
        }
        int selectedIndex = cloudFileList.getSelectionModel().getSelectedIndex();
        if (!HelperMethods.canSelectedFileBeDownloaded(selectedIndex, googleDriveFiles)) {
            return;
        }

        File selectedFile = googleDriveFiles.get(selectedIndex);
        String fileName = selectedFile.getName();
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        downloadService.prepareSelected(selectedFile);
        asyncActionService.run(
                UserAction.PRESSED_DOWNLOAD_SINGLE,
                downloadService::downloadSelected,
                downloadService::complete,
                exception -> downloadService.fail(exception, "download"));
    }

    @FXML
    public void onApplyConfigButtonClick() throws IOException {
        ConfigurationFileService configurationFileService = new ConfigurationFileService();
        configurationFileService.applyConfiguration(this);
    }

    @FXML
    public void onLaunchGameButtonClick() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "openjk.x86.exe",
                    launcherConfiguration.getCustomArguments()
            );
            //processBuilder.directory(new java.io.File(System.getProperty("user.dir")));
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

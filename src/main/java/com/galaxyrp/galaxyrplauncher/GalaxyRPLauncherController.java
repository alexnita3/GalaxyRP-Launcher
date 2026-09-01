package com.galaxyrp.galaxyrplauncher;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

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
    public TextField ResolutionYTestBox;
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
    public ListView cloudFileList;
    @FXML
    private Label welcomeText;

    @FXML
    public void onDownloadAllButtonClick() throws GeneralSecurityException, IOException {
        System.out.println("Download All Button clicked");

        DriveQuickstart.main();
    }
}

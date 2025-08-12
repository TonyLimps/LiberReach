package com.tonylimps.filerelay.windows;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {
    @FXML private Label languageLabel;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Label nameLabel;
    @FXML private TextField nameField;
    @FXML private Label downloadPathLabel;
    @FXML private TextField downloadPathField;
    @FXML private Button browseDownloadPathButton;
    @FXML private Label tokenLabel;
    @FXML private TextArea tokenArea;
    @FXML private Label tokenTimeRemainingLabel;
    @FXML private Label timeRemainingLabel;
}

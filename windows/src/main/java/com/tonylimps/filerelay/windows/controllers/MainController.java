package com.tonylimps.filerelay.windows;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainController {
    @FXML private Label nameLabel;
    @FXML private Label IPLabel;
    @FXML private Label portLabel;
    @FXML private Label authorizedLabel;
    @FXML private ListView<String> authorizedList;
    @FXML private ScrollBar authorizedScrollBar;
    @FXML private Label viewableLabel;
    @FXML private ListView<String> viewableList;
    @FXML private ScrollBar viewableScrollBar;
    @FXML private Button addDeviceButton;
    @FXML private Button backButton;
    @FXML private TextField pathField;
    @FXML private Button flushButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ListView<String> fileList;
    @FXML private Button downloadButton;
    @FXML private Button uploadButton;

}
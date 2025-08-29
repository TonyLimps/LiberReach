package org.tonylimps.filerelay.windows.controllers;

import org.tonylimps.filerelay.core.Core;
import org.tonylimps.filerelay.core.Profile;
import org.tonylimps.filerelay.windows.Main;
import org.tonylimps.filerelay.windows.managers.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class MainController {

	private final Logger logger = LogManager.getLogger(MainController.class);
	private static MainController instance;

	private double fixedWidth;
	private double fixedWidth2;
	private boolean ignoreWidthChange = false;
	private boolean ignoreWidthChange2 = false;

	@FXML
	private SplitPane mainSplitPane;
	@FXML
	private SplitPane mainSplitPane2;
	@FXML
	private MenuItem fileSettingsMenuItem;
	@FXML
	private MenuItem fileExitMenuItem;
	@FXML
	private MenuItem helpAboutMenuItem;
	@FXML
	private Label nameLabel;
	@FXML
	private Label addressLabel;
	@FXML
	private TextField addressField;
	@FXML
	private Label authorizedLabel;
	@FXML
	private ListView<String> authorizedList;
	@FXML
	private ScrollBar authorizedScrollBar;
	@FXML
	private Label viewableLabel;
	@FXML
	private ListView<String> viewableList;
	@FXML
	private ScrollBar viewableScrollBar;
	@FXML
	private Button addDeviceButton;
	@FXML
	private Button backButton;
	@FXML
	private TextField pathField;
	@FXML
	private Button flushButton;
	@FXML
	private TextField searchField;
	@FXML
	private Button searchButton;
	@FXML
	private ListView<String> fileList;
	@FXML
	private Button downloadButton;
	@FXML
	private Button uploadButton;

	@FXML
	private void initialize() {
		ObservableList<String> items = FXCollections.observableArrayList();
		items.add("sb");
		items.add("sb");
		items.add("sb");
		viewableList.setItems(items);
		instance = this;
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		Profile profile = Main.getProfileManager().getProfile();

		try {
			nameLabel.setText(bundle.getString("main.label.deviceName") + " " + profile.getDeviceName());
			addressField.setText(Core.getHostAddress()+":"+profile.getPort());
		}
		catch (UnknownHostException e) {
			addressField.setText("UnknownHostException");
			logger.error(e);
			Main.getExceptionManager().throwException(e);
		}

		Platform.runLater(() -> {
			WindowManager.getStage("main").setTitle(bundle.getString("main.title"));
			WindowManager.getStage("main").setOnCloseRequest( event -> Main.exit(0));

			fixedWidth = mainSplitPane.getDividerPositions()[0] * mainSplitPane.getWidth();
			mainSplitPane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
				if (!ignoreWidthChange) {
					fixedWidth = newVal.doubleValue() * mainSplitPane.getWidth();
				}
			});

			mainSplitPane.widthProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal.doubleValue() != oldVal.doubleValue()) {
					ignoreWidthChange = true;
					double newPosition = fixedWidth / newVal.doubleValue();
					mainSplitPane.setDividerPosition(0, newPosition);
					ignoreWidthChange = false;
				}
			});

			fixedWidth2 = (1-mainSplitPane2.getDividerPositions()[0]) * mainSplitPane2.getWidth();
			mainSplitPane2.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
				if (!ignoreWidthChange2) {
					fixedWidth2 = (1-newVal.doubleValue()) * mainSplitPane2.getWidth();
				}
			});

			mainSplitPane2.widthProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal.doubleValue() != oldVal.doubleValue()) {
					ignoreWidthChange2 = true;
					double newPosition = fixedWidth2 / newVal.doubleValue();
					mainSplitPane2.setDividerPosition(0, 1-newPosition);
					ignoreWidthChange2 = false;
				}
			});
		});
	}

	@FXML
	private void onFileSettingsButtonAction() {
		WindowManager.show("settings");
	}
	@FXML
	private void onFileExitButtonAction() {
		System.exit(0);
	}
	@FXML
	private void onAuthorizedListviewClick(MouseEvent e){
		System.out.println(e.toString());
		System.out.println(authorizedList.getSelectionModel().getSelectedItem());
	}
	@FXML
	private void onAddButtonAction() {
		WindowManager.show("add");
		Stage stage = WindowManager.getStage("add");
		stage.setOnCloseRequest(event -> {
			WindowManager.hide("add");
		});
	}

	public static MainController getInstance() {
		return instance;
	}
}

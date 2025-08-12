package com.tonylimps.filerelay.windows.controllers;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.windows.Main;
import com.tonylimps.filerelay.windows.managers.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class MainController {

	private static MainController instance;

	@FXML
	private MenuItem fileSettingsMenuItem;
	@FXML
	private MenuItem fileExitMenuItem;
	@FXML
	private MenuItem helpAboutMenuItem;
	@FXML
	private Label nameLabel;
	@FXML
	private Label IPLabel;
	@FXML
	private Label portLabel;
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
		instance = this;
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		Profile profile = Main.getProfileManager().getProfile();
		try {
			nameLabel.setText(bundle.getString("main.label.deviceName") + " " + profile.getDeviceName());
			IPLabel.setText(bundle.getString("main.label.deviceIP") + " " + Core.getHostAddress());
			portLabel.setText(bundle.getString("main.label.devicePort") + " " + profile.getPort());
		}
		catch (UnknownHostException e) {
			IPLabel.setText(bundle.getString("main.label.deviceIP") + " " + "UnknownHostException");
			Main.getExceptionManager().throwException(e);
		}
	}

	@FXML
	private void onFileSettingsButtonAction() {
		WindowManager.show("settings");
		Stage stage = WindowManager.getStage("settings");
		stage.setOnCloseRequest(event -> {
			WindowManager.hide("settings");
		});
		stage.setTitle(Main.getResourceBundleManager().getBundle().getString("settings.title"));
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

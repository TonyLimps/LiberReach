package com.tonylimps.filerelay.windows.controllers;

import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.windows.Main;
import com.tonylimps.filerelay.windows.managers.WindowManager;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsController {

	private HashMap<Locale, ResourceBundle> supportedResourceBundles;
	private Locale locale;

	private static SettingsController instance;

	@FXML
	private Label languageLabel;
	@FXML
	private ComboBox<String> languageComboBox;
	@FXML
	private Label nameLabel;
	@FXML
	private TextField nameField;
	@FXML
	private Label downloadPathLabel;
	@FXML
	private TextField downloadPathField;
	@FXML
	private Button browseDownloadPathButton;
	@FXML
	private Label tokenLabel;
	@FXML
	private TextArea tokenArea;
	@FXML
	private Label timeRemainingLabel;
	@FXML
	private Label portLabel;
	@FXML
	private TextField portField;

	@FXML
	private void initialize() {
		instance = this;
		supportedResourceBundles = Main.getResourceBundleManager().getSupportedResourceBundles();
		locale = Main.getProfileManager().getProfile().getLocale();
		languageComboBox.setValue(supportedResourceBundles.get(locale).getString("language"));
		Profile profile = Main.getProfileManager().getProfile();
		nameField.setText(profile.getDeviceName());
		portField.setText(String.valueOf(profile.getPort()));
	}

	@FXML
	private void onApplyButtonAction() {
		Profile profile = Main.getProfileManager().getProfile();
		profile.setLocale(locale);
		profile.setDeviceName(nameField.getText());
		if (isPortValid(Integer.parseInt(portLabel.getText()))) {
			profile.setPort(Integer.parseInt(portField.getText()));
		}
		Main.getProfileManager().saveProfile();
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		alert.setTitle(bundle.getString("settings.alert.apply.title"));
		alert.setHeaderText(null);
		alert.setGraphic(null);
		alert.setContentText(bundle.getString("settings.alert.apply.content"));
		alert.showAndWait();
		WindowManager.hide("settings");
	}

	@FXML
	private void onCancelButtonAction() {
		WindowManager.hide("settings");
	}

	@FXML
	private void onLanguageComboBoxAction() {
		String language = languageComboBox.getValue();
		supportedResourceBundles.values().stream()
								.filter(bundle -> bundle.getString("language").equals(language))
								.findFirst()
								.ifPresent(bundle -> {
											   locale = Locale.forLanguageTag(bundle.getString("locale"));
										   }
								);

	}

	@FXML
	private void flushSupportedLanguages() {
		languageComboBox.getItems().clear();
		supportedResourceBundles.values().stream()
								.forEach(bundle -> {
									languageComboBox.getItems().add(bundle.getString("language"));
								});
	}

	private boolean isPortValid(int port) {
		try {
			ServerSocket testSocket = new ServerSocket(port);
			testSocket.close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	public static SettingsController getInstance() {
		return instance;
	}

	public TextArea getTokenArea() {
		return tokenArea;
	}

	public Label getTimeRemainingLabel() {
		return timeRemainingLabel;
	}

}

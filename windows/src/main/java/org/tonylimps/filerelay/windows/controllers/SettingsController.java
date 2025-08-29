package org.tonylimps.filerelay.windows.controllers;

import org.tonylimps.filerelay.core.Profile;
import org.tonylimps.filerelay.windows.Main;
import org.tonylimps.filerelay.windows.managers.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class SettingsController {

	private boolean isSettingsChanged;

	private HashMap<Locale, ResourceBundle> supportedResourceBundles;
	private Locale locale;
	private String lastSelectedLanguage;

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
	private Button applyButton;
	@FXML
	private Button cancelButton;

	@FXML
	private void initialize() {
		instance = this;
		isSettingsChanged = false;
		applyButton.setDisable(true);
		supportedResourceBundles = Main.getResourceBundleManager().getSupportedResourceBundles();
		locale = Main.getProfileManager().getProfile().getLocale();
		languageComboBox.setValue(supportedResourceBundles.get(locale).getString("language"));
		lastSelectedLanguage = languageComboBox.getValue();
		Profile profile = Main.getProfileManager().getProfile();
		nameField.setText(profile.getDeviceName());
		portField.setText(String.valueOf(profile.getPort()));
		Platform.runLater(() -> {
			Stage stage = WindowManager.getStage("settings");
			stage.setOnCloseRequest(event -> {
				if(SettingsController.getInstance().isSettingsChanged()){
					SettingsController.getInstance().apply();
				}
				WindowManager.hide("settings");
			});
			stage.setTitle(Main.getResourceBundleManager().getBundle().getString("settings.title"));
			languageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
				if (Objects.isNull(newVal) || newVal.isEmpty()) {
					languageComboBox.setValue(lastSelectedLanguage);
				}
				lastSelectedLanguage = languageComboBox.getValue();
			});
		});
	}

	@FXML
	public void apply() {
		Profile profile = Main.getProfileManager().getProfile();
		profile.setLocale(locale);
		profile.setDeviceName(nameField.getText());
		int port = Integer.parseInt(portField.getText());
		if (isPortValid(port)) {
			profile.setPort(port);
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
		isSettingsChanged = true;
		applyButton.setDisable(false);
	}

	@FXML
	private void onLanguageComboBoxClick() {
		languageComboBox.getItems().clear();
		supportedResourceBundles = Main.getResourceBundleManager().getSupportedResourceBundles();
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

	public ComboBox<String> getLanguageComboBox() {
		return languageComboBox;
	}
	public boolean isSettingsChanged() {
		return isSettingsChanged;
	}
	public void setSettingsChanged(boolean isSettingsChanged) {
		this.isSettingsChanged = isSettingsChanged;
	}
}

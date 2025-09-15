package org.tonylimps.liberreach.windows.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.managers.WindowManager;

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

	@FXML public Label languageLabel;
	@FXML public ComboBox<String> languageComboBox;
	@FXML public Label nameLabel;
	@FXML public TextField nameField;
	@FXML public Label downloadPathLabel;
	@FXML public TextField downloadPathField;
	@FXML public Button browseDownloadPathButton;
	@FXML public Label tokenLabel;
	@FXML public TextArea tokenArea;
	@FXML public Label timeRemainingLabel;
	@FXML public Button applyButton;
	@FXML public Button cancelButton;

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

	public static SettingsController getInstance() {
		return instance;
	}
	public boolean isSettingsChanged() {
		return isSettingsChanged;
	}
	public void setSettingsChanged(boolean isSettingsChanged) {
		this.isSettingsChanged = isSettingsChanged;
	}
}

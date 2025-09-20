package org.tonylimps.liberreach.windows.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.managers.WindowManager;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsController {

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
	private HashMap<Locale, ResourceBundle> supportedResourceBundles;
	private Locale locale;
	private String lastSelectedLanguage;

	public static SettingsController getInstance() {
		return instance;
	}

	@FXML
	private void initialize() {
		instance = this;
		supportedResourceBundles = Main.getResourceBundleManager().getSupportedResourceBundles();
		locale = Main.getProfileManager().getProfile().getLocale();
		lastSelectedLanguage = languageComboBox.getValue();
		Profile profile = Main.getProfileManager().getProfile();
		Platform.runLater(() -> {
			Stage stage = WindowManager.getStage("settings");
			stage.setOnCloseRequest(event -> WindowManager.hide("settings"));
			stage.setTitle(Main.getResourceBundleManager().getBundle().getString("settings.title"));
			languageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal == null || newVal.isEmpty()) {
					languageComboBox.setValue(lastSelectedLanguage);
				}
				lastSelectedLanguage = languageComboBox.getValue();
			});
			nameField.setText(profile.getDeviceName());
			languageComboBox.setValue(supportedResourceBundles.get(locale).getString("language"));
			downloadPathField.setText(profile.getDefaultDownloadPath());
		});
	}

	@FXML
	public void apply() {
		Profile profile = Main.getProfileManager().getProfile();
		profile.setLocale(locale);
		profile.setDeviceName(nameField.getText());
		profile.setDefaultDownloadPath(downloadPathField.getText());
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
	private void onLanguageComboBoxClick() {
		languageComboBox.getItems().clear();
		supportedResourceBundles = Main.getResourceBundleManager().getSupportedResourceBundles();
		supportedResourceBundles.values().stream()
			.forEach(bundle -> {
				languageComboBox.getItems().add(bundle.getString("language"));
			});
	}

	@FXML
	private void onBrowseButtonAction() {
		Stage stage = WindowManager.getStage("settings");
		DirectoryChooser chooser = new DirectoryChooser();
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		chooser.setTitle(bundle.getString("settings.button.browse"));
		File file = chooser.showDialog(stage);
		String path = file.toPath().toAbsolutePath().toString();
		downloadPathField.setText(path);
	}
}

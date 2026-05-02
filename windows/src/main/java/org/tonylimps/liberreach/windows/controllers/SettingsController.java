package org.tonylimps.liberreach.windows.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.tonylimps.liberreach.core.Config;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.managers.WindowManager;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
	private ResourceBundle bundle;
	private Locale locale;
	private String lastSelectedLanguage;

	public static SettingsController getInstance() {
		return instance;
	}

	@FXML
	private void initialize() {
		instance = this;
		bundle = Main.getContext().bundleManager.getBundle();
		locale = Main.getContext().configManager.getConfig().getLocale();
		lastSelectedLanguage = languageComboBox.getValue();
		Config config = Main.getContext().configManager.getConfig();
		Platform.runLater(() -> {
			Stage stage = WindowManager.getStage("settings");
			stage.setOnCloseRequest(event -> WindowManager.hide("settings"));
			stage.setTitle(Main.getContext().bundleManager.getBundle().getString("settings.title"));
			languageComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal == null || newVal.isEmpty()) {
					languageComboBox.setValue(lastSelectedLanguage);
				}
				lastSelectedLanguage = languageComboBox.getValue();
			});
			nameField.setText(config.getDeviceName());
			languageComboBox.setValue(bundle.getString("language"));
			downloadPathField.setText(config.getDefaultDownloadPath());
		});
	}

	@FXML
	public void apply() {
		Config config = Main.getContext().configManager.getConfig();
		config.setLocale(locale);
		config.setDeviceName(nameField.getText());
		config.setDefaultDownloadPath(downloadPathField.getText());
		Main.getContext().configManager.saveConfig();
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
		Map<Locale,  ResourceBundle> supportedResourceBundles = Main.getContext().bundleManager.getSupportedResourceBundles();
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
		Map<Locale,  ResourceBundle> supportedResourceBundles = Main.getContext().bundleManager.getSupportedResourceBundles();
		supportedResourceBundles.values()
			.forEach(bundle -> {
				languageComboBox.getItems().add(bundle.getString("language"));
			});
	}

	@FXML
	private void onBrowseButtonAction() {
		Stage stage = WindowManager.getStage("settings");
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(bundle.getString("settings.button.browse"));
		File file = chooser.showDialog(stage);
		String path = file.toPath().toAbsolutePath().toString();
		downloadPathField.setText(path);
	}
}

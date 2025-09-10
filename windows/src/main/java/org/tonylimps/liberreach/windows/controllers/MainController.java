package org.tonylimps.liberreach.windows.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.*;
import org.tonylimps.liberreach.core.threads.ViewableCommandThread;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.annotations.FixedWidth;
import org.tonylimps.liberreach.windows.annotations.FixedWidthHandler;
import org.tonylimps.liberreach.windows.annotations.SingleFocus;
import org.tonylimps.liberreach.windows.annotations.SingleFocusHandler;
import org.tonylimps.liberreach.windows.managers.WindowManager;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import static org.tonylimps.liberreach.core.enums.CommandType.GETPATH;

public class MainController {

	private static MainController instance;
	private final Logger logger = LogManager.getLogger(MainController.class);
	@FXML @SingleFocus @FixedWidth public SplitPane mainSplitPane;

	/*
	搜索功能已暂时弃用
	@FXML @SingleFocus public SplitPane mainSplitPane2;
	@FXML @SingleFocus public TextField searchField;
	@FXML @SingleFocus public Button searchButton;
	*/

	@FXML @SingleFocus public MenuItem fileSettingsMenuItem;
	@FXML @SingleFocus public MenuItem fileExitMenuItem;
	@FXML @SingleFocus public MenuItem helpAboutMenuItem;
	@FXML @SingleFocus(focused = true) public Label nameLabel;
	@FXML @SingleFocus public TextField addressField;
	@FXML @SingleFocus public Label authorizedLabel;
	@FXML @SingleFocus(group = "1") public ListView<String> authorizedList;
	@FXML @SingleFocus public MenuItem authorizationMenuItem;
	@FXML @SingleFocus public ScrollBar authorizedScrollBar;
	@FXML @SingleFocus public Label viewableLabel;
	@FXML @SingleFocus(group = "1") public ListView<String> viewableList;
	@FXML @SingleFocus public ScrollBar viewableScrollBar;
	@FXML @SingleFocus public Button addDeviceButton;
	@FXML @SingleFocus public Button backButton;
	@FXML @SingleFocus public TextField pathField;
	@FXML @SingleFocus public Button flushButton;
	@FXML @SingleFocus public ListView<String> filesList;
	@FXML @SingleFocus public Button downloadButton;
	@FXML @SingleFocus public Button uploadButton;
	private CustomPath currentPath = new CustomPath("");
	private List<String> files = new ArrayList<>();
	private List<String> folders = new ArrayList<>();
	private List<String> errs = new ArrayList<>();
	public static MainController getInstance() {
		return instance;
	}

	@FXML
	private void initialize() {
		instance = this;
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		Profile profile = Main.getProfileManager().getProfile();

		try {
			nameLabel.setText(bundle.getString("main.label.deviceName") + " " + profile.getDeviceName());
			addressField.setText(Core.getHostAddress() + ":" + profile.getPort());
		}
		catch (UnknownHostException e) {
			addressField.setText("UnknownHostException");
			logger.error(e);
			Main.getExceptionManager().throwException(e);
		}

		Platform.runLater(() -> {
			WindowManager.getStage("main").setTitle(bundle.getString("main.title"));
			WindowManager.getStage("main").setOnCloseRequest(event -> Main.exit(0));
			SingleFocusHandler singleFocusHandler = new SingleFocusHandler();
			singleFocusHandler.handle(getInstance());
			FixedWidthHandler fixedWidthHandler = new FixedWidthHandler();
			fixedWidthHandler.handle(getInstance());
		});
	}

	@FXML
	private void onFileSettingsButtonAction() {
		WindowManager.show("settings");
	}
	@FXML
	private void onFileExitButtonAction() {
		Main.exit(0);
	}
	@FXML
	private void onAuthorizedListviewClick() {
		String name = authorizedList.getSelectionModel().getSelectedItem();
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		AuthorizedDevice authorizedDevice = Main.getProfileManager().getProfile().getAuthorizedDevices().values().stream()
			.filter(device -> device.getDeviceName().equals(name))
			.findFirst().orElse(null);
		if (Objects.isNull(authorizedDevice)) {
			authorizationMenuItem.setDisable(true);
			return;
		}
		if (authorizedDevice.isAuthorized()) {
			authorizationMenuItem.setDisable(false);
			authorizationMenuItem.setText(bundle.getString("main.contextMenu.revokeAuthorization"));
		}
		else {
			authorizationMenuItem.setDisable(false);
			authorizationMenuItem.setText(bundle.getString("main.contextMenu.giveAuthorization"));
		}
	}
	@FXML
	private void onViewableListviewClick() {
		String name = viewableList.getSelectionModel().getSelectedItem();
		if(Objects.nonNull(name) && !name.isEmpty()){
			ViewableDevice device = Main.getProfileManager().getProfile().getViewableDevices().get(name);
			if(Objects.isNull(device))return;
			ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
			if(!device.isAuthorized()){
				setErrs(List.of(bundle.getString("main.fileError.accessDenied")));
				return;
			}
			if(!device.isOnline()){
				setErrs(List.of(bundle.getString("main.fileError.notOnline")));
				return;
			}
			currentPath = new CustomPath(name + "::");
			setCurrentPath(currentPath);
		}
	}
	@FXML
	private void onRemoveMenuItemAction() {
		Main.getProfileManager().getProfile().removeAuthorizedDevice(authorizedList.getSelectionModel().getSelectedItem());
		Main.getProfileManager().saveProfile();
	}
	@FXML
	private void onFilesListViewClick(MouseEvent event) {
		if(event.getClickCount() == 2) {
			String folderName = filesList.getSelectionModel().getSelectedItem();
			if(folders.contains(folderName)) {
				CustomPath newPath = new CustomPath(currentPath);
				newPath.enter(folderName);
				setCurrentPath(newPath);
			}
		}
	}
	@FXML
	private void onPathFieldPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			updateCurrentPath();
		}
	}
	@FXML
	private void onBackButtonAction() {
		CustomPath newPath = new CustomPath(currentPath);
		newPath.back();
		setCurrentPath(newPath);
	}

	@FXML
	private void onFlushButtonAction() {
		updateCurrentPath();
	}
	@FXML
	private void onAuthorizationMenuItemAction() {
		String name = authorizedList.getSelectionModel().getSelectedItem();
		AuthorizedDevice device = Main.getProfileManager().getProfile().getAuthorizedDevices().get(name);
		device.setAuthorized(!device.isAuthorized());
		Main.getProfileManager().saveProfile();
	}
	@FXML
	private void onAddButtonAction() {
		WindowManager.show("add");
	}

	public void updateDevicesLists() {
		HashMap<String, ViewableDevice> viewableDevices = Main.getProfileManager().getProfile().getViewableDevices();
		HashMap<String, AuthorizedDevice> authorizedDevices = Main.getProfileManager().getProfile().getAuthorizedDevices();

		List<String> authorizedDeviceNames = authorizedDevices.values().stream()
			.map(AuthorizedDevice::getRemarkName)
			.toList();
		List<String> viewableDeviceNames = viewableDevices.values().stream()
			.map(ViewableDevice::getRemarkName)
			.toList();
		if(!authorizedDeviceNames.equals(authorizedList.getItems())){
			authorizedList.setItems(FXCollections.observableList(authorizedDeviceNames));
		}
		if(!viewableDeviceNames.equals(viewableList.getItems())){
			viewableList.setItems(FXCollections.observableList(viewableDeviceNames));
		}

		String offlineColor = Core.getConfig("offlineColor");
		String unauthorizedColor = Core.getConfig("unauthorizedColor");
		String authorizedColor = Core.getConfig("authorizedColor");

		viewableList.setCellFactory(param -> new ListCell<>() {
			@Override
			protected void updateItem(String name, boolean empty) {
				super.updateItem(name, empty);

				ViewableDevice device = viewableDevices.get(name);

				if (empty || name == null || device == null) {
					setText(null);
					setGraphic(null);
					setStyle("-fx-text-fill: " + offlineColor + ";");
					return;
				}

				setText(name);
				if (device.isOnline()) {
					if (device.isAuthorized()) {
						setStyle("-fx-text-fill: " + authorizedColor + ";");
					}
					else {
						setStyle("-fx-text-fill: " + unauthorizedColor + ";");
					}
				}
				else {
					setStyle("-fx-text-fill: " + offlineColor + ";");
				}
			}
		});

		authorizedList.setCellFactory(param -> new ListCell<>() {
			@Override
			protected void updateItem(String name, boolean empty) {
				super.updateItem(name, empty);

				AuthorizedDevice device = authorizedDevices.get(name);

				if (empty || name == null || device == null) {
					setText(null);
					setGraphic(null);
					setStyle("-fx-text-fill: " + offlineColor + ";");
					return;
				}
				setText(name);
				if (device.isAuthorized()) {
					setStyle("-fx-text-fill: " + authorizedColor + ";");
				}
				else {
					setStyle("-fx-text-fill: " + unauthorizedColor + ";");
				}
			}
		});
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public void setFolders(List<String> folders) {
		this.folders = folders;
	}

	public void setErrs(List<String> errs) {
		files = List.of();
		folders = List.of();
		this.errs = errs;
		updateFilesListview();
	}

	public void updateFilesListview() {
		List<String> filesAndFolders = new ArrayList<>();
		if(!folders.isEmpty()) {
			filesAndFolders.addAll(folders);
		}
		if(!files.isEmpty()) {
			filesAndFolders.addAll(files);
		}
		if(!errs.isEmpty()) {
			filesAndFolders.addAll(errs);
		}
		List<String> finalFilesAndFolders = filesAndFolders.stream()
			.filter(file -> !file.isEmpty())
			.collect(Collectors.toList());
		List<String> oldFilesAndFolders = filesList.getItems();
		if(finalFilesAndFolders.equals(oldFilesAndFolders)) {
			return;
		}
		Platform.runLater(() -> {
			filesList.setItems(FXCollections.observableArrayList(finalFilesAndFolders));
			filesList.setCellFactory(param -> new ListCell<>() {
				@Override
				protected void updateItem(String name, boolean empty) {
					super.updateItem(name, empty);
					setText(name);
					String fileColor = Core.getConfig("fileColor");
					String folderColor = Core.getConfig("folderColor");
					String errColor = Core.getConfig("errColor");
					if(Objects.isNull(name))return;
					if (files.contains(name)) {
						setStyle("-fx-text-fill: " + fileColor + ";");
					}
					else if (folders.contains(name)) {
						setStyle("-fx-text-fill: " + folderColor + ";");
					}
					else if (errs.contains(name)) {
						setStyle("-fx-text-fill: " + errColor + ";");
					}
				}
			});
		});
	}

	private void updateCurrentPath(){
		String fullPath = pathField.getText();
		Platform.runLater(() -> {
			pathField.setFocusTraversable(false);
			pathField.setFocusTraversable(true);
		});
		setCurrentPath(new CustomPath(fullPath));
	}

	private void setCurrentPath(CustomPath newPath) {
		try{
			String deviceName = newPath.getDeviceName();
			ViewableDevice device = Main.getProfileManager().getProfile().getViewableDevices().get(deviceName);
			if(Objects.isNull(device)) {
				pathField.setText(currentPath.toString());
				return;
			}
			ViewableCommandThread commandThread = device.getCommandThread();
			if(Objects.isNull(commandThread)) {
				pathField.setText(currentPath.toString());
				return;
			}
			String command = Core.createCommand(
				"type", GETPATH.getCode(),
				"path", newPath.toString());
			ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
			Platform.runLater(() -> {
				filesList.setCellFactory(param -> new ListCell<>() {
					@Override
					protected void updateItem(String name, boolean empty) {
						super.updateItem(name, empty);
						setText(name);
						String fileColor = Core.getConfig("fileColor");
						setStyle("-fx-text-fill: " + fileColor + ";");
					}
				});
				filesList.setItems(FXCollections.observableList(List.of(bundle.getString("main.wait"))));
			});
			commandThread.send(command);
			pathField.setText(newPath.toString());
			currentPath.setFullPath(newPath);
		}
		catch (Exception e) {
			logger.error("Set path failed.", e);
			setCurrentPath(currentPath);
		}
	}

}

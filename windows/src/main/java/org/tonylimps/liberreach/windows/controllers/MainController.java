package org.tonylimps.liberreach.windows.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.*;
import org.tonylimps.liberreach.core.enums.SortMethod;
import org.tonylimps.liberreach.core.threads.ViewableCommandThread;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.annotations.FixedWidth;
import org.tonylimps.liberreach.windows.annotations.FixedWidthHandler;
import org.tonylimps.liberreach.windows.annotations.SingleFocus;
import org.tonylimps.liberreach.windows.annotations.SingleFocusHandler;
import org.tonylimps.liberreach.windows.managers.WindowManager;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static org.tonylimps.liberreach.core.enums.CommandType.GETPATH;

import static org.tonylimps.liberreach.core.enums.SortMethod.*;

public class MainController {

	private static MainController instance;
	private final Logger logger = LogManager.getLogger(MainController.class);
	@FXML @SingleFocus @FixedWidth public SplitPane mainSplitPane;

	// TODO:
	// @FXML @SingleFocus public SplitPane mainSplitPane2;
	// @FXML @SingleFocus public TextField searchField;
	// @FXML @SingleFocus public Button searchButton;


	@FXML public ListView<CustomPath> pathsListView;

	@FXML @SingleFocus public MenuItem fileSettingsMenuItem;
	@FXML @SingleFocus public MenuItem fileExitMenuItem;
	// TODO: @FXML @SingleFocus public MenuItem helpAboutMenuItem;
	@FXML @SingleFocus(focused = true) public Label nameLabel;
	@FXML @SingleFocus public TextField addressField;
	@FXML @SingleFocus public TextField address6Field;
	@FXML @SingleFocus public Label authorizedLabel;
	@FXML @SingleFocus(group = "1") public ListView<AuthorizedDevice> authorizedList;
	@FXML @SingleFocus public MenuItem authorizationMenuItem;
	@FXML @SingleFocus public ScrollBar authorizedScrollBar;
	@FXML @SingleFocus public Label viewableLabel;
	@FXML @SingleFocus(group = "1") public ListView<ViewableDevice> viewableList;
	@FXML @SingleFocus public ScrollBar viewableScrollBar;
	@FXML @SingleFocus public Button addDeviceButton;
	@FXML @SingleFocus public Button backButton;
	@FXML @SingleFocus public TextField pathField;
	@FXML @SingleFocus public Button flushButton;
	@FXML @SingleFocus public Label pathsListViewLabel;
	@FXML @SingleFocus public Button downloadButton;
	// TODO: @FXML @SingleFocus public Button uploadButton;
	// TODO: @FXML @SingleFocus public Button requestRecords;

	// sort
	@FXML MenuItem sortByName;
	@FXML MenuItem sortByType;
	@FXML MenuItem sortBySizeI;
	@FXML MenuItem sortBySizeD;
	@FXML MenuItem sortByLastModifiedI;
	@FXML MenuItem sortByLastModifiedD;

	private CustomPath currentPath = new CustomPath("",false);
	private List<CustomPath> paths;
	private SortMethod sortMethod = NAME;
	private static final HashMap<SortMethod, Comparator<CustomPath>> comparators = new HashMap<>();

	public static MainController getInstance() {
		return instance;
	}

	@FXML
	private void initialize() {
		instance = this;
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		Profile profile = Main.getProfileManager().getProfile();

		comparators.put(NAME, Comparator
			.comparing(CustomPath::isDirectory)
			.reversed()
			.thenComparing(CustomPath::getFileName)
		);
		comparators.put(TYPE, Comparator
			.comparing(CustomPath::isDirectory)
			.reversed()
			.thenComparing(CustomPath::getType)
		);
		comparators.put(SIZEI, Comparator
			.comparing(CustomPath::isDirectory)
			.reversed()
			.thenComparing(CustomPath::getSize)
		);
		comparators.put(SIZED, Comparator
			.comparing(CustomPath::isDirectory)
			.reversed()
			.thenComparing(CustomPath::getSize)
			.reversed()
		);
		comparators.put(LASTMODIFIEDI, Comparator
			.comparing(CustomPath::isDirectory)
			.reversed()
			.thenComparing(CustomPath::getLastModified)
		);
		comparators.put(LASTMODIFIEDD, Comparator
			.comparing(CustomPath::isDirectory)
			.reversed()
			.thenComparing(CustomPath::getLastModified)
			.reversed()
		);

		Platform.runLater(() -> {
			try {
				nameLabel.setText(bundle.getString("main.label.deviceName") + " " + profile.getDeviceName());
				addressField.setText(Core.getHostAddress());
				String address6 = Core.getHostAddress6();
				address6Field.setText(address6 == null ? bundle.getString("main.label.haveNotIPv6Address") : address6);
			}
			catch (UnknownHostException e) {
				addressField.setText(e.getMessage());
				logger.error(e);
				Main.getExceptionManager().throwException(e);
			}
			WindowManager.getStage("main").setTitle(bundle.getString("main.title"));
			WindowManager.getStage("main").setOnCloseRequest(event -> Main.exit(0));
			SingleFocusHandler singleFocusHandler = new SingleFocusHandler();
			singleFocusHandler.handle(getInstance());
			FixedWidthHandler fixedWidthHandler = new FixedWidthHandler();
			fixedWidthHandler.handle(getInstance());
			sortByName.setOnAction(event -> {sortMethod = NAME;updateCurrentPath();});
			sortByType.setOnAction(event -> {sortMethod = TYPE;updateCurrentPath();});
			sortBySizeI.setOnAction(event -> {sortMethod = SIZEI;updateCurrentPath();});
			sortBySizeD.setOnAction(event -> {sortMethod = SIZED;updateCurrentPath();});
			sortByLastModifiedI.setOnAction(event -> {sortMethod = LASTMODIFIEDI;updateCurrentPath();});
			sortByLastModifiedD.setOnAction(event -> {sortMethod = LASTMODIFIEDD;updateCurrentPath();});
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
		AuthorizedDevice authorizedDevice = authorizedList.getSelectionModel().getSelectedItem();
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		if (authorizedDevice == null) {
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
	private void onViewableListviewClick(MouseEvent event) {
		if(event.getButton() == MouseButton.PRIMARY) {
			ViewableDevice viewableDevice = viewableList.getSelectionModel().getSelectedItem();
			if (viewableDevice == null) {
				return;
			}
			ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
			if (!viewableDevice.isAuthorized()) {
				Exception exception = new Exception(bundle.getString("main.fileError.accessDenied"));
				Main.getExceptionManager().throwException(exception);
				return;
			}
			if (!viewableDevice.isOnline()) {
				Exception exception = new Exception(bundle.getString("main.fileError.notOnline"));
				Main.getExceptionManager().throwException(exception);
				return;
			}
			currentPath = new CustomPath(viewableDevice.getRemarkName() + "::", false);
			setCurrentPath(currentPath);
		}
	}

	@FXML
	private void onRemoveAuthorizedDeviceMenuItemAction() {
		Main.getProfileManager().getProfile().removeAuthorizedDevice(authorizedList.getSelectionModel().getSelectedItem().getRemarkName());
		Main.getProfileManager().saveProfile();
	}

	@FXML
	private void onRemoveViewableDeviceMenuItemAction(){
		Main.getProfileManager().getProfile().removeViewableDevice(viewableList.getSelectionModel().getSelectedItem().getRemarkName());
		Main.getProfileManager().saveProfile();
	}

	@FXML
	private void onPathsListViewClick(MouseEvent event) {
		if (event.getClickCount() == 2) {
			CustomPath path = pathsListView.getSelectionModel().getSelectedItem();
			String name = path.getFileName();
			if (paths.contains(path) && path.isDirectory()) {
				setCurrentPath(currentPath.enter(name));
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
		setCurrentPath(currentPath.back());
	}

	@FXML
	private void onFlushButtonAction() {
		updateCurrentPath();
	}

	@FXML
	private void onAuthorizationMenuItemAction() {
		AuthorizedDevice device = authorizedList.getSelectionModel().getSelectedItem();
		device.setAuthorized(!device.isAuthorized());
		Main.getProfileManager().saveProfile();
	}

	@FXML
	private void onAddButtonAction() {
		WindowManager.show("add");
	}

	@FXML
	private void onDownloadButtonAction() {
		String fileName = pathsListView.getSelectionModel().getSelectedItem().getFileName();
		CustomPath path = currentPath.enter(fileName);
		Path targetPath = Paths.get(Main.getProfileManager().getProfile().getDefaultDownloadPath());
		String deviceName = currentPath.getDeviceName();
		ViewableDevice device = Main.getProfileManager().getProfile().getViewableDevices().get(deviceName);
		Core.downloadFile(device, path, targetPath);
	}

	public void setPaths(List<CustomPath> paths) {
		this.paths = paths;
		MainController.getInstance().updatePathsListview();
	}

	private void setCurrentPath(CustomPath newPath) {
		try {
			String deviceName = newPath.getDeviceName();
			ViewableDevice device = Main.getProfileManager().getProfile().getViewableDevices().get(deviceName);
			if (device == null) {
				setCurrentPath(currentPath);
				return;
			}
			ViewableCommandThread commandThread = device.getCommandThread();
			if (commandThread == null) {
				setCurrentPath(currentPath);
				return;
			}
			String command = Core.createCommand(
				"type", GETPATH,
				"path", newPath.toString()
			);
			ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
			setPathsListViewMessage(bundle.getString("main.wait"));
			commandThread.send(command);
			pathField.setText(newPath.toString());
			currentPath.setFullPath(newPath);
		}
		catch (Exception e) {
			logger.error("Set path failed.", e);
			setCurrentPath(currentPath);
		}
	}

	public void updatePathsListview() {
		Platform.runLater(() -> {
			SortedList<CustomPath> sortedList = new SortedList<>(FXCollections.observableArrayList(paths));
			sortedList.setComparator(comparators.get(sortMethod));
			pathsListView.setItems(sortedList);
			pathsListView.setCellFactory(param -> new ListCell<>() {
				@Override
				protected void updateItem(CustomPath path, boolean empty) {

					super.updateItem(path, empty);

					if (empty || path == null) {
						setText(null);
						setStyle(null);
						return;
					}

					setText(path.getFileName());
					String fileColor = Core.getConfig("fileColor");
					String folderColor = Core.getConfig("folderColor");

					if (path.isDirectory()) {
						setStyle("-fx-text-fill: " + folderColor + ";");
					}
					else {
						setStyle("-fx-text-fill: " + fileColor + ";");
					}
				}
			});
			showPathsListView();
		});
	}

	public void setSortMethod(SortMethod sortMethod) {
		this.sortMethod = sortMethod;
		MainController.getInstance().updateCurrentPath();
	}

	public void updateDevicesLists() {
		List<ViewableDevice> viewableDevices = Main.getProfileManager().getProfile().getViewableDevices().values().stream().toList();
		List<AuthorizedDevice> authorizedDevices = Main.getProfileManager().getProfile().getAuthorizedDevices().values().stream().toList();

		if (!viewableDevices.equals(viewableList.getItems())) {
			viewableList.setItems(FXCollections.observableList(viewableDevices));
		}
		if (!authorizedDevices.equals(authorizedList.getItems())) {
			authorizedList.setItems(FXCollections.observableList(authorizedDevices));
		}

		String offlineColor = Core.getConfig("offlineColor");
		String unauthorizedColor = Core.getConfig("unauthorizedColor");
		String authorizedColor = Core.getConfig("authorizedColor");

		viewableList.setCellFactory(param -> new ListCell<>() {
			@Override
			protected void updateItem(ViewableDevice device, boolean empty) {
				super.updateItem(device, empty);

				if (empty || device == null) {
					setText(null);
					setGraphic(null);
					setStyle("-fx-text-fill: " + offlineColor + ";");
					return;
				}

				setText(device.getRemarkName());
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
			protected void updateItem(AuthorizedDevice device, boolean empty) {
				super.updateItem(device, empty);


				if (empty || device == null) {
					setText(null);
					setGraphic(null);
					setStyle("-fx-text-fill: " + offlineColor + ";");
					return;
				}
				setText(device.getRemarkName());
				if (device.isAuthorized()) {
					setStyle("-fx-text-fill: " + authorizedColor + ";");
				}
				else {
					setStyle("-fx-text-fill: " + unauthorizedColor + ";");
				}
			}
		});
	}


	public void showPathsListView() {
		Platform.runLater(() -> {
			pathsListView.setVisible(true);
			pathsListViewLabel.setVisible(false);
		});
	}

	private void updateCurrentPath() {
		String fullPath = pathField.getText();
		Platform.runLater(() -> {
			pathField.setFocusTraversable(false);
			pathField.setFocusTraversable(true);
		});
		setCurrentPath(new CustomPath(fullPath,false));
	}

	public void setPathsListViewMessage(String message) {
		String color = Core.getConfig("messageColor");
		Platform.runLater(() -> {
			pathsListViewLabel.setText(message);
			pathsListViewLabel.setStyle("-fx-text-fill: " + color + ";");
		});
		showPathsListViewLabel();
	}

	public void setPathsListViewLabelError(Exception e) {
		String stackTrace = Core.getExceptionStackTrace(e);
		String color = Core.getConfig("errColor");
		Platform.runLater(() -> {
			pathsListViewLabel.setStyle("-fx-text-fill: " + color + ";");
			pathsListViewLabel.setText(stackTrace);
		});
		showPathsListViewLabel();
	}

	public void showPathsListViewLabel() {
		Platform.runLater(() -> {
			pathsListView.setVisible(false);
			pathsListViewLabel.setVisible(true);
		});
	}
}

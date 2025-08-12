package com.tonylimps.filerelay.windows.managers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class WindowManager {
	private static final HashMap<String, Pair<Parent, Object>> windows = new HashMap<>();
	private static final HashMap<String, Stage> stages = new HashMap<>();
	private static final HashMap<String, Boolean> windowStatus = new HashMap<>();

	public static void initWindow(String name, String fxmlPath, ResourceBundle bundle) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(WindowManager.class.getResource(fxmlPath), bundle);
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		Object controller = fxmlLoader.getController();
		initWindow(name, stage, root, controller);
	}

	public static void initWindow(String name, Stage stage, Parent root, Object controller) {
		windows.put(name, new Pair<>(root, controller));
		stages.put(name, stage);
		windowStatus.put(name, false);
	}

	public static void show(String name) {
		Stage stage = stages.get(name);
		stage.setIconified(false);
		if (windowStatus.get(name)) {
			stage.requestFocus();
		}
		else {
			stage.show();
		}
		windowStatus.put(name, true);
	}

	public static void hide(String name) {
		Stage stage = stages.get(name);
		stage.hide();
		windowStatus.put(name, false);
	}

	public static void close(String name) {
		Stage stage = stages.get(name);
		stage.close();
		windowStatus.put(name, false);
	}

	public static <T> T getController(String name) {
		Pair<Parent, Object> pair = windows.get(name);
		return (T) pair.getValue();
	}

	public static Stage getStage(String name) {
		return stages.get(name);
	}
}

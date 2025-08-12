package com.tonylimps.filerelay.windows.managers;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.windows.Main;
import com.tonylimps.filerelay.windows.controllers.ExceptionDialogController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class ExceptionDialog {

	private final double windowMoveDistance = 30;
	private int exceptions;

	public ExceptionDialog(Exception e, int exceptions) {
		String time = Core.getCurrentTime();
		this.exceptions = exceptions;
		String message = e.getMessage();
		String stackTrace = Core.getExceptionStackTrace(e);
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(
					getClass().getResource("exception.fxml")
				);
				ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
				loader.setResources(bundle);
				Parent root = loader.load();
				Stage stage = new Stage();
				stage.setScene(new Scene(root));
				stage.setTitle(bundle.getString("exception.title"));
				stage.setOnCloseRequest(event -> {
					close();
				});
				ExceptionDialogController controller = loader.getController();
				controller.setInfo(message, stackTrace, exceptions, time);
				WindowManager.initWindow("exception" + exceptions, stage, root, loader.getController());
			}
			catch (Exception ex) {
				e.printStackTrace();
				ex.printStackTrace();
				System.exit(1);
			}
		});
	}

	public void show() {
		Platform.runLater(() -> {
			Stage stage = WindowManager.getStage("exception" + exceptions);
			stage.show();
			stage.centerOnScreen();
			double x = stage.getX();
			double y = stage.getY();
			stage.setX(x + windowMoveDistance * exceptions);
			stage.setY(y + windowMoveDistance * exceptions);
		});
	}

	public void close() {
		Platform.runLater(() -> {
			WindowManager.close("exception" + exceptions);
		});
	}
}

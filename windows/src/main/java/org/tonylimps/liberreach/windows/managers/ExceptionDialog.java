package org.tonylimps.liberreach.windows.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.controllers.ExceptionDialogController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class ExceptionDialog {

	private final Logger logger = LogManager.getLogger(getClass());
	private final double windowMoveDistance = 30;
	private final int exceptions;

	public ExceptionDialog(Exception e, int exceptions) {
		this.exceptions = exceptions;
		String message = e.getMessage();
		String stackTrace = Core.getExceptionStackTrace(e);
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/fxmls/exception.fxml")
				);
				ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
				loader.setResources(bundle);
				Parent root = loader.load();
				Stage stage = new Stage();
				Scene scene = new Scene(root);
				scene.getStylesheets().add("/style.css");
				stage.setScene(scene);
				stage.setTitle(bundle.getString("exception.title"));
				stage.setOnCloseRequest(event -> close());
				ExceptionDialogController controller = loader.getController();
				controller.setInfo(message, stackTrace, exceptions);
				WindowManager.initWindow("exception" + exceptions, stage, root, loader.getController());
			}
			catch (Exception ex) {
				logger.fatal("Create exception dialog failed.",ex);
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

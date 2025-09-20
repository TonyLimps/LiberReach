package org.tonylimps.liberreach.windows.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.tonylimps.liberreach.windows.managers.WindowManager;

public class ExceptionDialogController {
	@FXML
	private Label messageLabel;
	@FXML
	private TextArea exceptionArea;


	private int exceptions;
	private String message;
	private String stackTrace;

	@FXML
	private void initialize() {
		messageLabel.setText(message);
		exceptionArea.setText(stackTrace);
	}

	@FXML
	public void onIgnoreButtonAction() {
		Platform.runLater(() -> WindowManager.close("exception" + exceptions));
	}

	@FXML
	public void onExitButtonAction() {
		System.exit(1);
	}

	public void setInfo(String message, String stackTrace, int exceptions) {
		this.exceptions = exceptions;
		this.message = message;
		this.stackTrace = stackTrace;
		messageLabel.setText(message);
		exceptionArea.setText(stackTrace);
	}
}

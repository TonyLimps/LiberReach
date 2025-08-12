package com.tonylimps.filerelay.windows.controllers;

import com.tonylimps.filerelay.windows.managers.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExceptionDialogController {
	@FXML
	private Label messageLabel;
	@FXML
	private TextArea exceptionArea;


	private int exceptions;
	private String time;
	private String message;
	private String stackTrace;

	@FXML
	private void initialize() {
		messageLabel.setText(message);
		exceptionArea.setText(stackTrace);
	}

	@FXML
	public void onSaveErrorLogButtonAction() throws IOException {
		String name = "err-" + time + "-" + exceptions + ".log";
		File log = new File(name);
		log.createNewFile();
		FileWriter fw = new FileWriter(log);
		fw.write(time + "\n" + stackTrace);
		fw.close();
		messageLabel.setText("Saved as " + name);
	}

	@FXML
	public void onIgnoreButtonAction() {
		Platform.runLater(() -> {
			WindowManager.close("exception" + exceptions);
		});
	}

	@FXML
	public void onExitButtonAction() {
		System.exit(1);
	}

	public void setInfo(String message, String stackTrace, int exceptions, String time) {
		this.time = time;
		this.exceptions = exceptions;
		this.message = message;
		this.stackTrace = stackTrace;
		messageLabel.setText(message);
		exceptionArea.setText(stackTrace);
	}
}

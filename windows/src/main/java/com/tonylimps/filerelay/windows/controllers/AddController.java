package com.tonylimps.filerelay.windows.controllers;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.enums.RequestResults;
import com.tonylimps.filerelay.windows.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddController {
	@FXML
	private TextField IPField;
	@FXML
	private TextField portField;
	@FXML
	private TextArea tokenArea;

	@FXML
	private void onAddButtonAction() {
		String ip = IPField.getText();
		int port = Integer.parseInt(portField.getText());
		String token = tokenArea.getText();
		int soTimeout = Integer.parseInt(Core.getConfig("soTimeout"));
		String result;

		try(Socket socket = new Socket(ip,port)){
			socket.setSoTimeout(soTimeout);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println(Core.createCommand("type", "add", "token", token));
			try{
				HashMap<String,String> answer = Core.parseCommand(in.readLine());
				if(Boolean.parseBoolean(answer.get("result"))) {
					result = RequestResults.SUCCESS;
				}
				else {
					result = RequestResults.WRONGTOKEN;
				}
			}
			catch(Exception e){
				result = RequestResults.TIMEOUT;
			}
		}
		catch(IOException e){
			result = RequestResults.OFFLINE;
		}
		Alert alert = getAlert(result);
		alert.showAndWait();
	}

	private static Alert getAlert(String result) {
		Alert alert = null;
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		switch(result){
			case RequestResults.SUCCESS:
				alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle(bundle.getString("add.alert.success.title"));
				alert.setContentText(bundle.getString("add.alert.success.content"));
			case RequestResults.WRONGTOKEN:
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.wrongToken.title"));
				alert.setContentText(bundle.getString("add.alert.wrongToken.content"));
			case RequestResults.TIMEOUT:
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.timeout.title"));
				alert.setContentText(bundle.getString("add.alert.timeout.content"));
			case RequestResults.OFFLINE:
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.offline.title"));
				alert.setContentText(bundle.getString("add.alert.offline.content"));
			case RequestResults.TOOMANYREQUESTS:
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.tooManyRequests.title"));
				alert.setContentText(bundle.getString("add.alert.tooManyRequests.content"));
		}
		return alert;
	}
}

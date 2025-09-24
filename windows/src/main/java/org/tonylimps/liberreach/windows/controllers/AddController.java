package org.tonylimps.liberreach.windows.controllers;

import com.alibaba.fastjson2.JSON;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.ViewableDevice;
import org.tonylimps.liberreach.core.enums.RequestResult;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.managers.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.ResourceBundle;

import static org.tonylimps.liberreach.core.enums.CommandType.ADD;


/*
 * 本机发送请求后直接把远程加入可查看设备
 * 毕竟所有可查看设备的授权状态都是可能变化的
 * 再用心跳命令请求在线状态和授权状态
 * 如果用户点击了可查看设备，就校验授权状态并根据其返回结果
 * 远程那边，收到请求后直接放进授权设备，可以对其调整授权状态，并附带消息提示
 */
public class AddController {


	private static final Logger logger = LogManager.getLogger(AddController.class);
	@FXML
	private TextField addressField;
	@FXML
	private TextArea tokenArea;

	@FXML
	private void initialize() {
		Platform.runLater(() -> {
			Stage stage = WindowManager.getStage("add");
			stage.setOnCloseRequest(event -> WindowManager.hide("add"));
			stage.setTitle(Main.getResourceBundleManager().getBundle().getString("add.title"));
		});
	}

	@FXML
	private void onAddButtonAction() {

		// 先给设备发送添加请求，如果令牌对了再让对面的用户同意
		// 中途出现错误视为设备不在线
		String host = addressField.getText();
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
		}
		catch (Exception e) {
			logger.error("Parse address {} failed.", host, e);
			Main.getExceptionManager().throwException(e);
			return;
		}

		String token = tokenArea.getText();
		int soTimeout = Integer.parseInt(Core.getConfig("soTimeout"));
		RequestResult result;
		int port = Integer.parseInt(Core.getConfig("defaultPort"));
		try (Socket socket = new Socket(host, port)) {
			logger.info("Connected to {}", address);

			socket.setSoTimeout(soTimeout);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String command = Core.createCommand("type", ADD, "token", token, "name", Main.getProfileManager().getProfile().getDeviceName());
			out.println(command);

			logger.info("Sent to {} :\n{}", address, command);
			try {
				HashMap<String, Object> answer = JSON.parseObject(in.readLine(), HashMap.class);
				result = RequestResult.valueOf((String)answer.get("content"));
				if (result.equals(RequestResult.SUCCESS)) {
					String name = (String)answer.get("name");
					Main.getProfileManager().getProfile().addViewableDevice(new ViewableDevice(host, port, name));
					Main.getProfileManager().saveProfile();
				}
			}
			catch (Exception e) {
				result = RequestResult.TIMEOUT;
			}
		}
		catch (IOException e) {
			logger.error("Connect failed.");
			logger.error(e);
			result = RequestResult.OFFLINE;
		}
		Alert alert = getAlert(result);
		alert.showAndWait();
	}

	private static Alert getAlert(RequestResult result) {
		Alert alert = null;
		ResourceBundle bundle = Main.getResourceBundleManager().getBundle();
		switch (result) {
			case SUCCESS -> {
				alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle(bundle.getString("add.alert.success.title"));
				alert.setContentText(bundle.getString("add.alert.success.content"));
			}
			case WRONGTOKEN -> {
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.wrongToken.title"));
				alert.setContentText(bundle.getString("add.alert.wrongToken.content"));
			}
			case TIMEOUT -> {
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.timeout.title"));
				alert.setContentText(bundle.getString("add.alert.timeout.content"));
			}
			case OFFLINE -> {
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.offline.title"));
				alert.setContentText(bundle.getString("add.alert.offline.content"));
			}
			case TOOMANYREQUESTS -> {
				alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(bundle.getString("add.alert.tooManyRequests.title"));
				alert.setContentText(bundle.getString("add.alert.tooManyRequests.content"));
			}
		}
		return alert;
	}
}

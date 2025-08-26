package com.tonylimps.filerelay.core.threads;

import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.core.Token;
import com.tonylimps.filerelay.core.ViewableDevice;
import com.tonylimps.filerelay.core.enums.CommandTypes;
import com.tonylimps.filerelay.core.enums.RequestResults;
import com.tonylimps.filerelay.core.managers.ExceptionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 负责与查看设备通信的命令线程
 */
public class ViewableCommandThread extends CommandThread{

	private final Logger logger = LogManager.getLogger(this.getClass());

	public ViewableCommandThread(
		Socket socket,
		ExceptionManager exceptionManager,
		Profile profile,
		AtomicBoolean running,
		Token token,
		ConnectThread connectThread) {
		try {
			this.address = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.profile = profile;
			this.running = running;
			this.exceptionManager = exceptionManager;
			this.token = token;
			this.connectThread = connectThread;
		}
		catch (IOException e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	@Override
	protected void exec(HashMap<String, String> command) throws IOException {
		logger.info("Received from "+address+" :\n" + command);
		if (command.get("type").equals(CommandTypes.ANSWER)) {// 收到回应
			switch (command.get("answerType")) {
				// 确认回应命令类型
				case CommandTypes.ADD -> {
					if (command.get("content").equals(RequestResults.SUCCESS)) {
						String name = command.get("name");
						profile.addViewableDevice(new ViewableDevice(address.getHostName(), address.getPort(), name));
					}
				}
				case CommandTypes.HEARTBEAT -> {
					ViewableDevice device = profile.getViewableDevices().get(address);
					if(Objects.nonNull(device)) {
						device.setAuthorized(Boolean.parseBoolean(command.get("isAuthorized")));
					}
				}
			}
		}
	}

	// 这个方法可以向可查看设备发送命令并更新在线状态
	public void send(String command) {
		System.out.println("Sent to "+address.toString()+" :\n" + command);
		// 发送命令
		out.println(command);
		// 如果PrintWriter没出错，则认为设备在线
		connectThread.getViewableDevices().get(address).setOnline(!out.checkError());
	}

}

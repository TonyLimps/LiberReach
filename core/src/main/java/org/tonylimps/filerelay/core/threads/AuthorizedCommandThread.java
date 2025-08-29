package org.tonylimps.filerelay.core.threads;

import org.tonylimps.filerelay.core.AuthorizedDevice;
import org.tonylimps.filerelay.core.Core;
import org.tonylimps.filerelay.core.Token;
import org.tonylimps.filerelay.core.enums.CommandTypes;
import org.tonylimps.filerelay.core.enums.RequestResults;
import org.tonylimps.filerelay.core.managers.ExceptionManager;
import org.tonylimps.filerelay.core.managers.ProfileManager;
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
 * 负责与授权设备通信的命令线程
 */
public class AuthorizedCommandThread extends CommandThread {

	private final Logger logger = LogManager.getLogger(this.getClass());

	public AuthorizedCommandThread(Socket socket,
								   ExceptionManager exceptionManager,
								   ProfileManager profileManager,
								   AtomicBoolean running,
								   Token token,
								   ConnectThread connectThread) {
		try {
			this.address = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.profileManager = profileManager;
			this.profile = profileManager.getProfile();
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
		switch (command.get("type")) {
			case CommandTypes.ADD -> {
				// 添加设备请求
				if (command.get("token").equals(token.getValue())) {
					// 如果令牌正确，回应允许命令
					answer(Core.createCommand(
						"type", CommandTypes.ANSWER,
						"answerType", CommandTypes.ADD,
						"name", profile.getDeviceName(),
						"host", Core.getHostAddress(),
						"port", profile.getPort().toString(),
						"content", RequestResults.SUCCESS
					));
					profile.addAuthorizedDevice(new AuthorizedDevice(address, command.get("name")));
				}
				else {
					// 如果令牌错误，回应拒绝命令
					answer(Core.createCommand(
						"type", CommandTypes.ANSWER,
						"answerType", CommandTypes.ADD,
						"host", Core.getHostAddress(),
						"port", profile.getPort().toString(),
						"content", RequestResults.WRONGTOKEN
					));
				}
			}
			case CommandTypes.HEARTBEAT -> {
				AuthorizedDevice device = profile.getAuthorizedDevices().get(address);
				if(Objects.nonNull(device)){
					answer(Core.createCommand(
						"type", CommandTypes.ANSWER,
						"answerType", CommandTypes.HEARTBEAT,
						"isAuthorized", String.valueOf(device.isAuthorized())
					));
				}
			}
		}
	}

	public void answer(String command) {
		logger.info("Answered to "+address.toString()+" :\n" + command);
		out.println(command);
	}

}

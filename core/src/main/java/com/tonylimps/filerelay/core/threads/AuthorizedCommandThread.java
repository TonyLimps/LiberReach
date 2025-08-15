package com.tonylimps.filerelay.core.threads;

import com.tonylimps.filerelay.core.AuthorizedDevice;
import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.core.Token;
import com.tonylimps.filerelay.core.enums.CommandTypes;
import com.tonylimps.filerelay.core.enums.RequestResults;
import com.tonylimps.filerelay.core.managers.ExceptionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuthorizedCommandThread extends CommandThread {

	public AuthorizedCommandThread(Socket socket,
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
			exceptionManager.throwException(e);
		}
	}
	@Override
	protected void exec(HashMap<String, String> command) throws IOException {
		Core.getLogger().info("Received from "+address.toString()+" :\n" + command);
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
				AuthorizedDevice device = connectThread.getAuthorizedDevices().get(address);
				if(Objects.nonNull(device)) {
					device.setOnline(true);
					device.setCommandThread(this);
				}
			}
		}
	}

	public void answer(String command) {
		Core.getLogger().info("Answered to "+address.toString()+" :\n" + command);
		out.println(command);
		connectThread.getAuthorizedDevices().get(address).setOnline(!out.checkError());
	}

}

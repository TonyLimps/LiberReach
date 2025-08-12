package com.tonylimps.filerelay.core.threads;

import com.alibaba.fastjson2.JSON;
import com.tonylimps.filerelay.core.*;
import com.tonylimps.filerelay.core.managers.ExceptionManager;
import com.tonylimps.filerelay.core.enums.CommandTypes;
import com.tonylimps.filerelay.core.enums.RequestResults;

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
								   AtomicBoolean debug,
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
			this.debug = debug;
		}
		catch (IOException e) {
			exceptionManager.throwException(e);
		}
	}
	@Override
	protected void exec(HashMap<String, String> command) throws IOException {
		if(debug.get()) {
			System.out.println("Received from "+address.toString()+" :\n" + command);
		}
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
		if(debug.get()) {
			System.out.println("Answered to "+address.toString()+" :\n" + command);
		}
		out.println(command);
		connectThread.getAuthorizedDevices().get(address).setOnline(!out.checkError());
	}

}

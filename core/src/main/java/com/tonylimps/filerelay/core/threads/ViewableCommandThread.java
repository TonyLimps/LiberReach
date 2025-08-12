package com.tonylimps.filerelay.core.threads;

import com.alibaba.fastjson2.JSON;
import com.tonylimps.filerelay.core.*;
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

public class ViewableCommandThread extends CommandThread{

	public ViewableCommandThread(
		Socket socket,
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
			case CommandTypes.ANSWER -> {
				// 收到回应
				switch (command.get("answerType")) {
					// 确认回应命令类型
					case CommandTypes.ADD -> {
						if (command.get("content").equals(RequestResults.SUCCESS)) {
							String name = command.get("name");
							profile.addViewableDevice(new ViewableDevice(address.getHostName(), address.getPort(), name));
						}
					}
				}
			}
		}
	}

	// 这个方法可以向可查看设备发送命令并更新在线状态
	public void send(String command) {
		if(debug.get()) {
			System.out.println("Sent to "+address.toString()+" :\n" + command);
		}
		// 发送命令
		out.println(command);
		// 如果PrintWriter没出错，则认为设备在线
		connectThread.getViewableDevices().get(address).setOnline(!out.checkError());
	}

}

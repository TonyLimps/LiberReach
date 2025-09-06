package org.tonylimps.filerelay.core.threads;

import com.alibaba.fastjson2.JSON;
import org.tonylimps.filerelay.core.Token;
import org.tonylimps.filerelay.core.ViewableDevice;
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
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 负责与查看设备通信的命令线程
 */
public class ViewableCommandThread extends CommandThread{

	private final Logger logger = LogManager.getLogger(getClass());
	private ViewableDevice device;

	public ViewableCommandThread(
		ViewableDevice device,
		ExceptionManager exceptionManager,
		ProfileManager profileManager,
		AtomicBoolean running,
		Token token,
		UpdateThread updateThread) {
		try {
			this.device = device;
			this.address = device.getAddress();
			Socket socket = new Socket(address.getHostString(),address.getPort());
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.profileManager = profileManager;
			this.profile = profileManager.getProfile();
			this.running = running;
			this.exceptionManager = exceptionManager;
			this.token = token;
			this.updateThread = updateThread;
		}
		catch (IOException e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	@Override
	protected void exec(HashMap<String, String> command) throws IOException {
		if (command.get("type").equals(CommandTypes.ANSWER)) {
			// 收到回应
			switch (command.get("answerType")) {
				// 确认回应命令类型
				case CommandTypes.ADD -> {
					if (command.get("content").equals(RequestResults.SUCCESS)) {
						String name = command.get("name");
						profile.addViewableDevice(new ViewableDevice(address.getHostString(), address.getPort(), name));
					}
				}
				case CommandTypes.HEARTBEAT -> {
					ViewableDevice viewableDevice = profile.getViewableDevices().get(device.getRemarkName());
					if(Objects.nonNull(viewableDevice)) {
						viewableDevice.setAuthorized(Boolean.parseBoolean(command.get("isAuthorized")));
					}
				}
				case CommandTypes.GETPATH -> {
					try {
						//Thread.sleep(1000);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
					updateThread.setFilesList(JSON.parseArray(command.get("files"),String.class),
											  JSON.parseArray(command.get("folders"),String.class),
											  JSON.parseArray(command.get("errs"), String.class)
					);
				}
			}
		}
	}

	// 这个方法可以向可查看设备发送命令并更新在线状态
	public void send(String command) {
		// 发送命令
		out.println(command);
		// 如果PrintWriter没出错，则认为设备在线

		if(!command.contains("\"type\":\"1\"")){
			// 如果不是心跳命令就写进日志
			logger.info("Sent to {}:\n{}", address, command);
		}

		if(out.checkError()) {
			device.setOnline(false);
			device.setCommandThread(null);
		}
		else{
			device.setOnline(true);
			device.setCommandThread(this);
		}
	}

}

package org.tonylimps.liberreach.core.threads;

import com.alibaba.fastjson2.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.CustomPath;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.ViewableDevice;
import org.tonylimps.liberreach.core.enums.CommandType;
import org.tonylimps.liberreach.core.enums.RequestResult;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ProfileManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.tonylimps.liberreach.core.enums.CommandType.ANSWER;

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
			Socket socket = new Socket(address.getHostAddress(), Integer.parseInt(Core.getConfig("defaultPort")));
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
		CommandType type = CommandType.fromCode(command.get("type"));
		if (type == ANSWER) {
			// 收到回应
			CommandType answerType = CommandType.fromCode(command.get("answerType"));
			switch (answerType) {
				// 确认回应命令类型
				case ADD -> {
					if (command.get("content").equals(RequestResult.SUCCESS.getCode())) {
						String name = command.get("name");
						profile.addViewableDevice(new ViewableDevice(address.getHostAddress(), Integer.parseInt(Core.getConfig("defaultPort")), name));
					}
				}
				case HEARTBEAT -> {
					ViewableDevice viewableDevice = profile.getViewableDevices().get(device.getRemarkName());
					if(Objects.nonNull(viewableDevice)) {
						viewableDevice.setOnline(Boolean.parseBoolean(command.get("online")));
						viewableDevice.setAuthorized(Boolean.parseBoolean(command.get("isAuthorized")));
					}
				}
				case GETPATH -> {
					if(Boolean.parseBoolean(command.get("err"))) {
						Exception exception = JSON.parseArray(command.get("exception"), IOException.class).get(0);
						exceptionManager.throwException(exception);
					}
					else{
						List<CustomPath> paths = JSON.parseArray(command.get("paths"), String.class).stream()
							.map(CustomPath::fromJSONString)
							.toList();
						updateThread.setPaths(paths);
					}
				}
			}
		}
	}

	@Override
	protected void error(Exception e){
		logger.error(e);
		device.setOnline(false);
		device.setCommandThread(null);
	}

}

package org.tonylimps.liberreach.core.threads;

import com.alibaba.fastjson2.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.*;
import org.tonylimps.liberreach.core.enums.CommandType;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ProfileManager;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.tonylimps.liberreach.core.enums.CommandType.*;
import static org.tonylimps.liberreach.core.enums.RequestResult.SUCCESS;
import static org.tonylimps.liberreach.core.enums.RequestResult.WRONGTOKEN;

/*
 * 负责与授权设备通信的命令线程
 */
public class AuthorizedCommandThread extends CommandThread {

	private final Logger logger = LogManager.getLogger(getClass());

	public AuthorizedCommandThread(Socket socket,
								   ExceptionManager exceptionManager,
								   ResourceBundle bundle,
								   ProfileManager profileManager,
								   AtomicBoolean running,
								   Token token,
								   UpdateThread updateThread) {
		try {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.profileManager = profileManager;
			this.profile = profileManager.getProfile();
			this.running = running;
			this.exceptionManager = exceptionManager;
			this.token = token;
			this.bundle = bundle;
			this.updateThread = updateThread;
			this.address = socket.getInetAddress();
		}
		catch (IOException e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}
	@Override
	protected void exec(HashMap<String, Object> command) throws IOException {
		CommandType type = CommandType.valueOf((String)command.get("type"));
		switch (type) {
			case ADD -> {
				// 添加设备请求
				if (command.get("token").equals(token.getValue())) {
					// 如果令牌正确，回应允许命令
					send(Core.createCommand(
						"type", ANSWER,
						"answerType", ADD,
						"name", profile.getDeviceName(),
						"host", Core.getHostAddress(),
						"port", Core.getConfig("defaultPort"),
						"content", SUCCESS
					));
					profile.addAuthorizedDevice(new AuthorizedDevice(address, (String)command.get("name")));
					profileManager.saveProfile();
				}
				else {
					// 如果令牌错误，回应拒绝命令
					send(Core.createCommand(
						"type", ANSWER,
						"answerType", ADD,
						"host", Core.getHostAddress(),
						"port", Core.getConfig("defaultPort"),
						"content", WRONGTOKEN
					));
				}
			}
			case HEARTBEAT -> {
				boolean isAuthorized = isAuthorized();
				send(Core.createCommand(
					"type", ANSWER,
					"answerType", HEARTBEAT,
					"online", true,
					"authorized", isAuthorized
				));
			}
			case GETPATH -> {
				if(!isAuthorized()){
					return;
				}
				CustomPath customPath = new CustomPath((String)command.get("path"),true);
				List<CustomPath> paths;
				boolean err = false;
				IOException exception = null;
				if(customPath.isOnlyDeviceName()){
					paths = Arrays.stream(File.listRoots())
						.map(file -> new CustomPath("",file.toPath(),true))
						.toList();
				}
				else{
					Path path = customPath.getPath();
					try{
						paths = Files.list(path)
							.map(p -> new CustomPath("",p,true))
							.toList();
					}
					catch(IOException e){
						err = true;
						paths = List.of();
						exception = e;
						logger.error(e);
					}
				}
				send(Core.createCommand(
					"type", ANSWER,
					"answerType", GETPATH,
					"paths", JSON.toJSONString(paths),
					"err", err,
					"exception", exception == null
						? ""
						: JSON.toJSONString(List.of(exception))
				));
			}
			case DOWNLOAD -> {
				if(!isAuthorized()){
					return;
				}
				CustomPath customPath = new CustomPath((String)command.get("path"),false);
				Path path = customPath.getPath();
				int port = (int)command.get("port");
				File file = path.toFile();
				FileSender fileSender = new FileSender(file, address, port, exceptionManager);
				fileSender.createFile();
				fileSender.connect();
				fileSender.start();
			}
		}
	}

	private boolean isAuthorized() {
		AuthorizedDevice device = profile.getAuthorizedDevices().values().stream()
			.filter(d -> d.getAddress().equals(address))
			.findFirst().orElse(null);
		if(device != null){
			return device.isAuthorized();
		}
		return false;
	}
}

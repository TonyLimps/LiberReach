package org.tonylimps.filerelay.core.threads;

import com.alibaba.fastjson2.JSON;
import org.tonylimps.filerelay.core.AuthorizedDevice;
import org.tonylimps.filerelay.core.Core;
import org.tonylimps.filerelay.core.CustomPath;
import org.tonylimps.filerelay.core.Token;
import org.tonylimps.filerelay.core.enums.CommandTypes;
import org.tonylimps.filerelay.core.enums.RequestResults;
import org.tonylimps.filerelay.core.managers.ExceptionManager;
import org.tonylimps.filerelay.core.managers.ProfileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
			this.address = new InetSocketAddress(socket.getInetAddress(),socket.getPort());
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
					profile.addAuthorizedDevice(new AuthorizedDevice(address.getAddress(), command.get("name")));
					profileManager.saveProfile();
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
				AuthorizedDevice device = profile.getAuthorizedDevices().values().stream()
					.filter(d -> d.getAddress().equals(address.getAddress()))
					.findFirst().orElse(null);
				if(Objects.nonNull(device)){
					answer(Core.createCommand(
						"type", CommandTypes.ANSWER,
						"answerType", CommandTypes.HEARTBEAT,
						"isAuthorized", String.valueOf(device.isAuthorized())
					));
				}
			}
			case CommandTypes.GETPATH -> {
				AuthorizedDevice device = profile.getAuthorizedDevices().values().stream()
					.filter(d -> d.getAddress().equals(address.getAddress()))
					.findFirst().orElse(null);
				if(Objects.nonNull(device)){
					boolean isAuthorized = device.isAuthorized();
					String unAuthorized = JSON.toJSONString(List.of(bundle.getString("main.notAuthorized")));
					CustomPath path = new CustomPath(command.get("path"));
					List<String> files;
					List<String> folders;
					List<String> errs;
					boolean err = false;

					if(path.isOnlyDeviceName()){
						files = new ArrayList();
						folders = Arrays.stream(File.listRoots())
							.map(File::getAbsolutePath)
							.toList();
						errs = new ArrayList();
					}
					else{
						try{
							files = Files.list(Paths.get(path.getPath()))
								.filter(Files::isRegularFile)
								.map(f -> f.getFileName().toString())
								.collect(Collectors.toList());
							folders = Files.list(Paths.get(path.getPath()))
								.filter(Files::isDirectory)
								.map(f -> f.getFileName().toString())
								.collect(Collectors.toList());
							errs = new ArrayList();
							if(folders.isEmpty() && files.isEmpty()){
								err = true;
								errs = List.of(bundle.getString("main.fileError.emptyDirectory"));
							}
						}
						catch(Exception e){
							err = true;
							files = new ArrayList();
							folders = new ArrayList();
							String errName;
							if(e instanceof NoSuchFileException){
								errName = bundle.getString("main.fileError.noSuchFile");
								errs = List.of(errName,e.getMessage());
							}
							else if(e instanceof NotDirectoryException){
								errName = bundle.getString("main.fileError.notDirectory");
								errs = List.of(errName,e.getMessage());
							}
							else if(e instanceof AccessDeniedException || e instanceof SecurityException){
								errName = bundle.getString("main.fileError.accessDenied");
								errs = List.of(errName,e.getMessage());
							}
							else{
								errs = List.of(e.getMessage());
							}
						}
					}
					answer(Core.createCommand(
						"type", CommandTypes.ANSWER,
						"answerType", CommandTypes.GETPATH,
						"folders", isAuthorized
							? JSON.toJSONString(folders)
							: unAuthorized,
						"files", isAuthorized
							? JSON.toJSONString(files)
							: unAuthorized,
						"err", String.valueOf(err),
						"errs", JSON.toJSONString(errs)
					));
				}
			}
		}
	}

	public void answer(String command) {
		if(!command.contains("\"answerType\":\"1\"")){
			// 不是心跳命令就写进日志
			logger.info("Answered to {} :\n{}", address.toString(), command);
		}
		out.println(command);
	}
}

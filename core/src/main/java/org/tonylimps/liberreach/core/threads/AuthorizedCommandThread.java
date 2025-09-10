package org.tonylimps.liberreach.core.threads;

import com.alibaba.fastjson2.JSON;
import org.tonylimps.liberreach.core.AuthorizedDevice;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.CustomPath;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.enums.CommandType;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ProfileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
			this.address = new InetSocketAddress(socket.getInetAddress(),socket.getPort());
		}
		catch (IOException e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}
	@Override
	protected void exec(HashMap<String, String> command) throws IOException {
		CommandType type = CommandType.fromCode(command.get("type"));
		switch (type) {
			case ADD -> {
				// 添加设备请求
				if (command.get("token").equals(token.getValue())) {
					// 如果令牌正确，回应允许命令
					send(Core.createCommand(
						"type", ANSWER.getCode(),
						"answerType", ADD.getCode(),
						"name", profile.getDeviceName(),
						"host", Core.getHostAddress(),
						"port", profile.getPort().toString(),
						"content", SUCCESS.getCode()
					));
					profile.addAuthorizedDevice(new AuthorizedDevice(address.getAddress(), command.get("name")));
					profileManager.saveProfile();
				}
				else {
					// 如果令牌错误，回应拒绝命令
					send(Core.createCommand(
						"type", ANSWER.getCode(),
						"answerType", ADD.getCode(),
						"host", Core.getHostAddress(),
						"port", profile.getPort().toString(),
						"content", WRONGTOKEN.getCode()
					));
				}
			}
			case HEARTBEAT -> {
				AuthorizedDevice device = profile.getAuthorizedDevices().values().stream()
					.filter(d -> d.getAddress().equals(address.getAddress()))
					.findFirst().orElse(null);
				if(Objects.nonNull(device)){
					send(Core.createCommand(
						"type", ANSWER.getCode(),
						"answerType", HEARTBEAT.getCode(),
						"online", "true",
						"isAuthorized", String.valueOf(device.isAuthorized())
					));
				}
			}
			case GETPATH -> {
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
					send(Core.createCommand(
						"type", ANSWER.getCode(),
						"answerType", GETPATH.getCode(),
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
			case DOWNLOAD -> {
				CustomPath customPath = new CustomPath(command.get("path"));
				String path = customPath.getPath();

			}
		}
	}

}

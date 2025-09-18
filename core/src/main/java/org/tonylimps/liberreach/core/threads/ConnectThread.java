package org.tonylimps.liberreach.core.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.*;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ProfileManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;


/*
 * 这是一个用于管理所有套接字连接的线程
 * 当它启动时，会创建一个ServerSocket，并将所有连接过来的Socket分配到一个授权设备命令线程
 * 已授权设备会主动连接可查看设备，并主动给它发送各种命令
 * 可查看设备接收命令并执行，然后回复给已授权设备
 */

public class ConnectThread extends Thread {

	private final Logger logger = LogManager.getLogger(getClass());
	private final AtomicBoolean running;

	private final ExceptionManager exceptionManager;
	private final ProfileManager profileManager;
	private final ResourceBundle bundle;
	private final Profile profile;
	private final Token token;
	private final UpdateThread updateThread;
	private final HashMap<String, ViewableDevice> viewableDevices;
	private final HashMap<String, AuthorizedDevice> authorizedDevices;
	private ServerSocket serverSocket;

	public ConnectThread(
		ExceptionManager exceptionManager,
		ResourceBundle resourceBundle,
		AtomicBoolean running,
		ProfileManager profileManager,
		Token token,
		UpdateThread updateThread
	)
	{
		this.exceptionManager = exceptionManager;
		this.profileManager = profileManager;
		this.bundle = resourceBundle;
		this.running = running;
		this.token = token;
		this.profile = profileManager.getProfile();
		this.updateThread = updateThread;
		this.viewableDevices = profile.getViewableDevices();
		this.authorizedDevices = profile.getAuthorizedDevices();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(Integer.parseInt(Core.getConfig("defaultPort")));
			while (running.get()) {
				// 不断接收套接字，并分配授权设备线程
				// 根据规范，授权设备会主动连接查看设备
				// 如果配置文件中有这个设备，就把命令线程和设备关联
				Socket socket = serverSocket.accept();
				AuthorizedCommandThread authorizedCommandThread = new AuthorizedCommandThread(socket, exceptionManager, bundle, profileManager, running, token, updateThread);
				authorizedCommandThread.start();
				InetSocketAddress address = new InetSocketAddress(socket.getInetAddress().getHostAddress(), socket.getPort());
				AuthorizedDevice device = authorizedDevices.get(address);
				if (device != null) {
					device.setCommandThread(authorizedCommandThread);
				}
			}
		}
		catch (IOException e) {
			if (running.get()) {
				logger.error(e);
				exceptionManager.throwException(e);
			}
		}
	}

	public HashMap<String, ViewableDevice> getViewableDevices() {
		return viewableDevices;
	}

	public HashMap<String, AuthorizedDevice> getAuthorizedDevices() {
		return authorizedDevices;
	}

	public void close() {
		try {
			serverSocket.close();
			authorizedDevices.values().stream()
				.map(AuthorizedDevice::getCommandThread)
				.filter(Objects::nonNull)
				.forEach(CommandThread::close);
			viewableDevices.values().stream()
				.map(ViewableDevice::getCommandThread)
				.filter(Objects::nonNull)
				.forEach(CommandThread::close);
		}
		catch (IOException e) {

		}
		interrupt();
		try {
			join();
		}
		catch (InterruptedException e) {
			logger.info("Connect thread interrupted.");
		}
		logger.info("Connect thread closed.");
	}
}

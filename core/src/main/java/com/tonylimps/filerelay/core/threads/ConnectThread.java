package com.tonylimps.filerelay.core.threads;

import com.tonylimps.filerelay.core.AuthorizedDevice;
import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.core.Token;
import com.tonylimps.filerelay.core.ViewableDevice;
import com.tonylimps.filerelay.core.managers.ExceptionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


/*
 * 这是一个用于管理所有套接字连接的线程
 * 当它启动时，会创建一个ServerSocket，并将所有连接过来的Socket分配到一个授权设备命令线程
 * 已授权设备会主动连接可查看设备，并主动给它发送各种命令
 * 可查看设备接收命令并执行，然后回复给已授权设备
 */

public class ConnectThread extends Thread {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final AtomicBoolean running;

	private final ExceptionManager exceptionManager;
	private final Profile profile;
	private final Token token;
	private ServerSocket serverSocket;

	private final HashMap<InetSocketAddress, ViewableDevice> viewableDevices;
	private final HashMap<InetSocketAddress, AuthorizedDevice> authorizedDevices;

	public ConnectThread(ExceptionManager exceptionManager,
						 AtomicBoolean running,
						 Profile profile,
						 Token token) {
		this.exceptionManager = exceptionManager;
		this.profile = profile;
		this.running = running;
		this.token = token;
		this.viewableDevices = profile.getViewableDevices();
		this.authorizedDevices = profile.getAuthorizedDevices();
	}

	@Override
	public void run() {
		try{
			serverSocket = new ServerSocket(profile.getPort());
			while (running.get()) {
				// 不断接收套接字，并分配授权设备线程
				// 根据规范，授权设备会主动连接查看设备
				// 如果配置文件中有这个设备，就把命令线程和设备关联
				Socket socket = serverSocket.accept();
				AuthorizedCommandThread authorizedCommandThread = new AuthorizedCommandThread(socket, exceptionManager, profile, running, token, this);
				authorizedCommandThread.start();
				InetSocketAddress address = new InetSocketAddress(socket.getInetAddress().getHostAddress(), socket.getPort());
				AuthorizedDevice device = authorizedDevices.get(address);
				if (Objects.nonNull(device)) {
					device.setCommandThread(authorizedCommandThread);
				}
			}
		}
		catch (IOException e) {
			if(running.get()){
				logger.error(e);
				exceptionManager.throwException(e);
			}
		}
	}

	public HashMap<InetSocketAddress, ViewableDevice> getViewableDevices() {
		return viewableDevices;
	}

	public HashMap<InetSocketAddress, AuthorizedDevice> getAuthorizedDevices() {
		return authorizedDevices;
	}

	public void close() {
		try{
			serverSocket.close();
			authorizedDevices.values().stream()
							 .map(AuthorizedDevice::getCommandThread)
							 .forEach(CommandThread::close);
			viewableDevices.values().stream()
						   .map(ViewableDevice::getCommandThread)
						   .forEach(CommandThread::close);
		}
		catch (IOException e) {

		}
		interrupt();
		try{
			join();
		}
		catch (InterruptedException e) {
			logger.info("Connect thread interrupted.");
		}
		logger.info("Connect thread closed.");
	}
}

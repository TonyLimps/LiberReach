package com.tonylimps.filerelay.core.threads;

import com.tonylimps.filerelay.core.*;
import com.tonylimps.filerelay.core.managers.ExceptionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/* 这是一个用于管理所有套接字连接的线程
 * 当它启动时，会创建一个ServerSocket，并将所有连接过来的Socket分配到一个授权设备命令线程
 * 已授权设备会主动连接可查看设备，并主动给它发送各种命令
 * 可查看设备接收命令并执行，然后回复给已授权设备
 */

public class ConnectThread extends Thread {

	private final AtomicBoolean running;

	private final AtomicBoolean debug;
	private final ExceptionManager exceptionManager;
	private final Profile profile;
	private final Token token;

	private final HashMap<InetSocketAddress, ViewableDevice> viewableDevices;
	private final HashMap<InetSocketAddress, AuthorizedDevice> authorizedDevices;

	public ConnectThread(ExceptionManager exceptionManager,
						 AtomicBoolean running,
						 Profile profile,
						 Token token,
						 AtomicBoolean debug) {
		this.exceptionManager = exceptionManager;
		this.profile = profile;
		this.running = running;
		this.token = token;
		this.viewableDevices = profile.getViewableDevices();
		this.authorizedDevices = profile.getAuthorizedDevices();
		this.debug = debug;
	}

	@Override
	public void run() {
		try(ServerSocket serverSocket = new ServerSocket(profile.getPort())) {
			while (running.get()) {
				Socket socket = serverSocket.accept();
				AuthorizedCommandThread authorizedCommandThread = new AuthorizedCommandThread(socket, exceptionManager, profile, running, token, debug, this);
				authorizedCommandThread.start();
			}
		}
		catch (IOException e) {
			exceptionManager.throwException(e);
		}
	}

	public HashMap<InetSocketAddress, ViewableDevice> getViewableDevices() {
		return viewableDevices;
	}

	public HashMap<InetSocketAddress, AuthorizedDevice> getAuthorizedDevices() {
		return authorizedDevices;
	}

	public AuthorizedCommandThread getCommandThreadFromAuthorized(InetSocketAddress address){
		return authorizedDevices.get(address).getCommandThread();
	}

	public ViewableCommandThread getCommandThreadFromViewable(InetSocketAddress address) {
		return viewableDevices.get(address).getCommandThread();
	}
}

package com.tonylimps.filerelay.windows.threads;

import com.tonylimps.filerelay.core.Device;
import com.tonylimps.filerelay.core.ExceptionManager;
import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.windows.Main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ConnectThread extends Thread {

	private final ExceptionManager exceptionManager;

	private final Profile profile;

	private HashMap<String, Device> connectedViewableDevices;
	private HashMap<String, Device> connectedAuthorizedDevices;

	public ConnectThread(ExceptionManager exceptionManager) {
		this.exceptionManager = exceptionManager;
		this.profile = Main.getProfileManager().getProfile();
	}


	@Override
	public void run() {
		try(ServerSocket serverSocket = new ServerSocket(profile.getPort())) {
			while (Main.isRunning()) {
				Socket socket = serverSocket.accept();
				new CommandThread(socket, exceptionManager).start();
			}
		}
		catch (IOException e) {
			exceptionManager.throwException(e);
		}
	}
}

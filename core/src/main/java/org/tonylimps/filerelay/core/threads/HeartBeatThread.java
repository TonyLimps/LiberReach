package org.tonylimps.filerelay.core.threads;

import org.tonylimps.filerelay.core.Core;
import org.tonylimps.filerelay.core.Profile;
import org.tonylimps.filerelay.core.Token;
import org.tonylimps.filerelay.core.enums.CommandTypes;
import org.tonylimps.filerelay.core.managers.ExceptionManager;
import org.tonylimps.filerelay.core.managers.ProfileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 这是心跳线程，每隔一段时间向查看设备发送心跳命令
 * 用于确保socket正常连接，并更新设备的在线状态和授权状态
 */
public class HeartBeatThread extends Thread {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final AtomicBoolean running;

	private final Profile profile;
	private final ProfileManager profileManager;
	private final ConnectThread connectThread;
	private final int heartBeatDelayMillis;
	private final ExceptionManager exceptionManager;
	private final Token token;

	public HeartBeatThread(
		ExceptionManager exceptionManager,
		AtomicBoolean running,
		ProfileManager profileManager,
		Token token,
		ConnectThread connectThread
	){
		this.profileManager = profileManager;
		this.profile = profileManager.getProfile();
		this.running = running;
		this.connectThread = connectThread;
		this.exceptionManager = exceptionManager;
		this.heartBeatDelayMillis = Integer.parseInt(Core.getConfig("heartBeatDelayMillis"));
		this.token = token;
	}

	@Override
	public void run() {
		while (running.get()) {
			try {
				// 每隔一段时间遍历可查看设备，发送心跳命令，send方法会自动更新在线状态
				profile.getViewableDevices().values()
					   .forEach(device -> {
						   try {
							   ViewableCommandThread commandThread = device.getCommandThread();
							   if (Objects.isNull(commandThread)) {
								   InetSocketAddress address = device.getAddress();
								   commandThread = new ViewableCommandThread(
									   new Socket(address.getAddress(), address.getPort()),
									   exceptionManager,
									   profileManager,
									   running,
									   token,
									   connectThread
								   );
								   commandThread.start();
							   }
							   commandThread.send(Core.createCommand("type", CommandTypes.HEARTBEAT));
						   }
						   catch (Exception e) {
								logger.error(e);
								exceptionManager.throwException(e);
						   }
					   });
				Thread.sleep(heartBeatDelayMillis);
			}
			catch (InterruptedException e) {
				logger.info("Heartbeat thread interrupted.");
			}
		}
	}

	public void close(){
		interrupt();
		try{
			join();
		}
		catch (InterruptedException e) {
			logger.info("Heartbeat thread interrupted.");
		}
		logger.info("Heartbeat thread closed.");
	}
}

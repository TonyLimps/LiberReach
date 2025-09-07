package org.tonylimps.liberreach.core.threads;

import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.enums.CommandTypes;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ProfileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 这是心跳线程，每隔一段时间向查看设备发送心跳命令
 * 用于确保socket正常连接，并更新设备的在线状态和授权状态
 */
public class HeartBeatThread extends Thread {

	private final Logger logger = LogManager.getLogger(getClass());
	private final AtomicBoolean running;

	private final Profile profile;
	private final ProfileManager profileManager;
	private final UpdateThread updateThread;
	private final int heartBeatDelayMillis;
	private final ExceptionManager exceptionManager;
	private final Token token;

	public HeartBeatThread(
		ExceptionManager exceptionManager,
		AtomicBoolean running,
		ProfileManager profileManager,
		Token token,
		UpdateThread updateThread
	){
		this.profileManager = profileManager;
		this.profile = profileManager.getProfile();
		this.running = running;
		this.updateThread = updateThread;
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
						   ViewableCommandThread commandThread = device.getCommandThread();
						   try {
							   if (Objects.isNull(commandThread)) {
								   InetSocketAddress address = device.getAddress();
								   commandThread = new ViewableCommandThread(
									   device,
									   exceptionManager,
									   profileManager,
									   running,
									   token,
									   updateThread
								   );
								   commandThread.start();
							   }
							   commandThread.send(Core.createCommand("type", CommandTypes.HEARTBEAT));
						   }
						   catch (Exception e) {
							   commandThread.error(e);
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

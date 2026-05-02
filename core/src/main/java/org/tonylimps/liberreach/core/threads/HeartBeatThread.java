package org.tonylimps.liberreach.core.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.AppContext;
import org.tonylimps.liberreach.core.Util;
import org.tonylimps.liberreach.core.Config;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ConfigManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.tonylimps.liberreach.core.enums.CommandType.HEARTBEAT;

/*
 * 这是心跳线程，每隔一段时间向查看设备发送心跳命令
 * 用于确保socket正常连接，并更新设备的在线状态和授权状态
 */
public class HeartBeatThread extends Thread {

	private final Logger logger = LogManager.getLogger(getClass());
	private final AtomicBoolean running;

	private final Config config;
	private final ConfigManager configManager;
	private final UpdateThread updateThread;
	private final int heartBeatDelayMillis;
	private final ExceptionManager exceptionManager;
	private final Token token;

	public HeartBeatThread(AppContext context, UpdateThread updateThread)
	{
		this.configManager = context.configManager;
		this.config = configManager.getConfig();
		this.running = context.running;
		this.updateThread = updateThread;
		this.exceptionManager = context.exceptionManager;
		this.token = context.token;
		this.heartBeatDelayMillis = Util.getAppConfig("heartBeatDelayMillis", int.class);
	}

	@Override
	public void run() {
		while (running.get()) {
			try {
				// 每隔一段时间遍历可查看设备，发送心跳命令，send方法会自动更新在线状态
				config.getViewableDevices().values()
					.forEach(device -> {
						ViewableCommandThread commandThread = device.getCommandThread();
						try {
							if (commandThread == null) {
								commandThread = new ViewableCommandThread(
									device,
									exceptionManager,
									configManager,
									running,
									token,
									updateThread
								);
								commandThread.start();
								device.setCommandThread(commandThread);
							}
							commandThread.send(Util.createCommand("type", HEARTBEAT));
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

	public void close() {
		interrupt();
		try {
			join();
		}
		catch (InterruptedException e) {
			logger.info("Heartbeat thread interrupted.");
		}
		logger.info("Heartbeat thread closed.");
	}
}

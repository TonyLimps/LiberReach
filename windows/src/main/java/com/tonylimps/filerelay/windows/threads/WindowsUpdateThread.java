package com.tonylimps.filerelay.windows.threads;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.managers.ExceptionManager;
import com.tonylimps.filerelay.core.threads.UpdateThread;
import com.tonylimps.filerelay.windows.Main;
import com.tonylimps.filerelay.windows.controllers.MainController;
import com.tonylimps.filerelay.windows.controllers.SettingsController;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


/*
 * 这个线程用于更新UI
 */

public class WindowsUpdateThread extends UpdateThread {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final ExceptionManager exceptionManager;
	private final int updateDelayMillis;
	private final AtomicBoolean running;

	public WindowsUpdateThread(ExceptionManager exceptionManager, AtomicBoolean running) {
		this.needToFlushToken = new AtomicBoolean(true);
		this.exceptionManager = exceptionManager;
		this.running = running;
		updateDelayMillis = Integer.parseInt(Core.getConfig("uiUpdateDelayMillis"));
	}

	@Override
	public void run() {
		while (running.get()) {
			try {
				Thread.sleep(updateDelayMillis);
			}
			catch (InterruptedException e) {
				logger.error(e);
				exceptionManager.throwException(e);
			}
			Platform.runLater(() -> {
				MainController mainController = MainController.getInstance();
				SettingsController settingsController = SettingsController.getInstance();
				if(needToFlushToken.get()) {
					settingsController.getTokenArea().setText(Main.getTokenThread().getToken().getValue());
					needToFlushToken.set(false);
				}
				settingsController.getTimeRemainingLabel().setText(String.valueOf(Main.getTokenThread().getTimeRemaining()));
			});
		}
	}
}

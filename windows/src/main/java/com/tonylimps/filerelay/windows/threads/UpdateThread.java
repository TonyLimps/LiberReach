package com.tonylimps.filerelay.windows.threads;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.managers.ExceptionManager;
import com.tonylimps.filerelay.windows.Main;
import com.tonylimps.filerelay.windows.controllers.MainController;
import com.tonylimps.filerelay.windows.controllers.SettingsController;
import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;


/*
 * 这个线程用于更新UI
 */

public class UpdateThread extends Thread {

	private final ExceptionManager exceptionManager;
	private final int updateDelayMillis;
	private final AtomicBoolean running;

	public UpdateThread(ExceptionManager exceptionManager, AtomicBoolean running) {
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
				exceptionManager.throwException(e);
			}
			Platform.runLater(() -> {
				MainController mainController = MainController.getInstance();
				SettingsController settingsController = SettingsController.getInstance();


				settingsController.getTokenArea().setText(Main.getTokenThread().getToken().getValue());
				settingsController.getTimeRemainingLabel().setText(String.valueOf(Main.getTokenThread().getTimeRemaining()));
			});
		}
	}
}

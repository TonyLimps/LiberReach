package org.tonylimps.liberreach.windows.threads;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.CustomPath;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.threads.UpdateThread;
import org.tonylimps.liberreach.windows.Main;
import org.tonylimps.liberreach.windows.controllers.MainController;
import org.tonylimps.liberreach.windows.controllers.SettingsController;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/*
 * 这个线程用于更新UI
 * 逻辑层和交互层的桥梁
 */

public class WindowsUpdateThread extends UpdateThread {

	private final Logger logger = LogManager.getLogger(getClass());

	public WindowsUpdateThread(ExceptionManager exceptionManager, AtomicBoolean running, Profile profile) {
		this.exceptionManager = exceptionManager;
		this.running = running;
		this.profile = profile;
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
				updateDevicesLists();
				updateTokenText();
			});
		}
	}

	private void updateDevicesLists() {
		MainController.getInstance().updateDevicesLists();
	}

	private void updateTokenText() {
		SettingsController settingsController = SettingsController.getInstance();
		String originalToken = settingsController.tokenArea.getText();
		String newToken = Main.getTokenThread().getToken().getValue();
		if (!originalToken.equals(newToken)) {
			settingsController.tokenArea.setText(newToken);
		}
		settingsController.timeRemainingLabel.setText(String.valueOf(Main.getTokenThread().getTimeRemaining()));
	}

	@Override
	public void setPaths(List<CustomPath> paths) {
		MainController.getInstance().setPaths(paths);
	}
}

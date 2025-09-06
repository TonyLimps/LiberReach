package org.tonylimps.filerelay.windows.threads;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.filerelay.core.AuthorizedDevice;
import org.tonylimps.filerelay.core.Core;
import org.tonylimps.filerelay.core.Profile;
import org.tonylimps.filerelay.core.ViewableDevice;
import org.tonylimps.filerelay.core.managers.ExceptionManager;
import org.tonylimps.filerelay.core.threads.UpdateThread;
import org.tonylimps.filerelay.windows.Main;
import org.tonylimps.filerelay.windows.controllers.MainController;
import org.tonylimps.filerelay.windows.controllers.SettingsController;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/*
 * 这个线程用于更新UI
 */

public class WindowsUpdateThread extends UpdateThread {

	private final Logger logger = LogManager.getLogger(getClass());

	public WindowsUpdateThread(ExceptionManager exceptionManager, AtomicBoolean running, Profile profile) {
		this.needToFlushToken = new AtomicBoolean(true);
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

	@Override
	public void setFilesList(List<String> filesList, List<String> foldersList, List<String> errs) {
		MainController.getInstance().setFiles(filesList);
		MainController.getInstance().setFolders(foldersList);
		MainController.getInstance().setErrs(errs);
		MainController.getInstance().updateFilesListview();
	}


	private void updateDevicesLists(){
		MainController mainController = MainController.getInstance();
		mainController.updateDevicesLists();
	}

	private void updateTokenText(){
		SettingsController settingsController = SettingsController.getInstance();
		if (needToFlushToken.get()) {
			settingsController.getTokenArea().setText(Main.getTokenThread().getToken().getValue());
			needToFlushToken.set(false);
		}
		settingsController.getTimeRemainingLabel().setText(String.valueOf(Main.getTokenThread().getTimeRemaining()));
	}
}

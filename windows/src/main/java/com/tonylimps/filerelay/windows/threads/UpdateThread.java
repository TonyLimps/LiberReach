package com.tonylimps.filerelay.core.threads;

import com.tonylimps.filerelay.core.ExceptionManager;
import com.tonylimps.filerelay.windows.Main;
import com.tonylimps.filerelay.windows.controllers.SettingsController;
import javafx.application.Platform;

public class UpdateThread extends Thread{

	private final ExceptionManager exceptionManager;
	private final int updateDelayMillis;

	public UpdateThread(ExceptionManager exceptionManager) {
		this.exceptionManager = exceptionManager;
		updateDelayMillis = Integer.parseInt(Main.getConfig("uiUpdateDelayMillis"));
	}

	@Override
	public void run() {
		while(Main.isRunning()){
			try{
				Thread.sleep(updateDelayMillis);
			}
			catch(InterruptedException e){
				exceptionManager.throwException(e);
			}
			Platform.runLater(()->{
				SettingsController.getInstance().getTokenArea().setText(Main.getTokenThread().getToken());
				SettingsController.getInstance().getTimeRemainingLabel().setText(String.valueOf(Main.getTokenThread().getTimeRemaining()));
			});
		}
	}
}

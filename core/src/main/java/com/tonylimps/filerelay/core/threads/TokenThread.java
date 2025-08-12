package com.tonylimps.filerelay.windows.threads;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.ExceptionManager;
import com.tonylimps.filerelay.windows.Main;
import com.tonylimps.filerelay.windows.controllers.SettingsController;
import javafx.application.Platform;

public class TokenThread extends Thread{

	private String uuid;
	private String token;
	private int timeRemaining;
	private final ExceptionManager exceptionManager;
	private final int flushDelaySeconds;


	public TokenThread(ExceptionManager exceptionManager){
		this.exceptionManager = exceptionManager;
		try{
			uuid = Main.getProfileManager().getUUID();
		}
		catch(Exception e){
			exceptionManager.throwException(e);
		}
		flushDelaySeconds = Integer.parseInt(Main.getConfig("tokenFlushDelaySeconds"));
	}

	public String getToken() {
		return token;
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}


	@Override
	public void run(){
		while(Main.isRunning()){
			try{
				token = Core.createToken(uuid);
				timeRemaining = flushDelaySeconds;
				for(int t = 0; t < flushDelaySeconds; t++){
					Thread.sleep(1000);
					timeRemaining -= 1;
				}
			}
			catch(Exception e){
				exceptionManager.throwException(e);
			}
		}
	}

}

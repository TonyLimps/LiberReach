package com.tonylimps.filerelay.core.threads;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.Token;
import com.tonylimps.filerelay.core.managers.ExceptionManager;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 这个线程用于更新token
 * 每隔一段时间(config.tokenFlushDelaySeconds)更新一次token
 */

public class TokenThread extends Thread {

	private final AtomicBoolean running;

	private final Token token;
	private final ExceptionManager exceptionManager;
	private final int flushDelaySeconds;
	private int timeRemaining;


	public TokenThread(ExceptionManager exceptionManager, AtomicBoolean running) {
		this.exceptionManager = exceptionManager;
		this.running = running;
		flushDelaySeconds = Integer.parseInt(Core.getConfig("tokenFlushDelaySeconds"));
		token = new Token();
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}

	public Token getToken() {
		return token;
	}

	@Override
	public void run() {
		while (running.get()) {
			try {
				token.flush();
				timeRemaining = flushDelaySeconds;
				for (int t = 0; t < flushDelaySeconds; t++) {
					Thread.sleep(1000);
					timeRemaining -= 1;
				}
			}
			catch(NoSuchAlgorithmException e){
				exceptionManager.throwException(e);
			}
			catch (InterruptedException e) {

			}
		}
	}

	public void close(){
		interrupt();
		try{
			join();
		}
		catch (InterruptedException e) {
			Core.getLogger().info("Token thread interrupted.");
		}
		Core.getLogger().info("Token thread closed.");
	}
}

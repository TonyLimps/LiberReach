package org.tonylimps.liberreach.core.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.managers.ExceptionManager;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 这个线程用于更新token
 * 每隔一段时间(config.tokenFlushDelaySeconds)更新一次token
 */

public class TokenThread extends Thread {

	private final Logger logger = LogManager.getLogger(getClass());
	private final AtomicBoolean running;

	private final Token token;
	private final ExceptionManager exceptionManager;
	private final UpdateThread updateThread;
	private final int flushDelaySeconds;
	private int timeRemaining;


	public TokenThread(ExceptionManager exceptionManager, AtomicBoolean running, UpdateThread updateThread) {
		this.exceptionManager = exceptionManager;
		this.running = running;
		this.updateThread = updateThread;
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
				logger.error(e);
				exceptionManager.throwException(e);
			}
			catch (InterruptedException e) {
				logger.info("Token thread interrupted.");
			}
		}
	}

	public void close(){
		interrupt();
		try{
			join();
		}
		catch (InterruptedException e) {
			logger.info("Token thread interrupted.");
		}
		logger.info("Token thread closed.");
	}
}

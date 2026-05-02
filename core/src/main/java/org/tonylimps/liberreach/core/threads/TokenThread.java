package org.tonylimps.liberreach.core.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.AppContext;
import org.tonylimps.liberreach.core.Util;
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
	private final int flushDelaySeconds;
	private int timeRemaining;


	public TokenThread(AppContext context) {
		this.exceptionManager = context.exceptionManager;
		this.running = context.running;
		flushDelaySeconds = Util.getAppConfig("tokenRefreshDelaySeconds", int.class);
		token = context.token;
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
				token.refresh();
				timeRemaining = flushDelaySeconds;
				for (int t = 0; t < flushDelaySeconds; t++) {
					Thread.sleep(1000);
					timeRemaining -= 1;
				}
			}
			catch (NoSuchAlgorithmException e) {
				logger.error(e);
				exceptionManager.throwException(e);
			}
			catch (InterruptedException e) {
				logger.info("Token thread interrupted.");
			}
		}
	}

	public void close() {
		interrupt();
		try {
			join();
		}
		catch (InterruptedException e) {
			logger.info("Token thread interrupted.");
		}
		logger.info("Token thread closed.");
	}
}

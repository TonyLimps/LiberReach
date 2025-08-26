package com.tonylimps.filerelay.core.threads;

import com.alibaba.fastjson2.JSON;
import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.core.Token;
import com.tonylimps.filerelay.core.managers.ExceptionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandThread extends Thread{

	private final Logger logger = LogManager.getLogger(this.getClass());
	protected AtomicBoolean running;

	protected Profile profile;
	protected BufferedReader in;
	protected PrintWriter out;
	protected InetSocketAddress address;
	protected ExceptionManager exceptionManager;
	protected Token token;
	protected ConnectThread connectThread;

	public CommandThread(){}

	@Override
	public void run() {
		while (running.get()) {
			try {
				String commandString = in.readLine();
				System.out.println(commandString);
				HashMap<String, String> command = JSON.parseObject(commandString, HashMap.class);
				exec(command);
			}
			catch (IOException e) {
				logger.error(e);
				exceptionManager.throwException(e);
			}
		}
	}

	protected void exec(HashMap<String, String> command) throws IOException {}


	public void close(){
		interrupt();
		try{
			join();
		}
		catch (InterruptedException e) {
			logger.info("Command thread "+getName()+" interrupted.");
		}
		logger.info("Command thread "+getName()+" closed.");
	}
}

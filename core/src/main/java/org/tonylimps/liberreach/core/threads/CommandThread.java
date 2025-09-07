package org.tonylimps.liberreach.core.threads;

import com.alibaba.fastjson2.JSON;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ProfileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandThread extends Thread{

	private final Logger logger = LogManager.getLogger(getClass());
	protected AtomicBoolean running;

	protected ProfileManager profileManager;
	protected Profile profile;
	protected ResourceBundle bundle;
	protected BufferedReader in;
	protected PrintWriter out;
	protected InetSocketAddress address;
	protected ExceptionManager exceptionManager;
	protected Token token;
	protected UpdateThread updateThread;

	public CommandThread(){}

	@Override
	public void run() {
		while (running.get()) {
			try {
				String commandString = in.readLine();
				if(Objects.isNull(commandString)){
					break;
				}
				if(! ( commandString.contains("\"type\":\"1\"") || commandString.contains("\"answerType\":\"1\"") )){
					// 如果命令不是心跳命令就写入日志
					logger.info("Received from {} :\n{}", address, commandString);
				}
				HashMap<String, String> command = JSON.parseObject(commandString, HashMap.class);
				exec(command);
			}
			catch (IOException e) {
				error(e);
				break;
			}
		}
	}

	protected void exec(HashMap<String, String> command) throws IOException {}

	protected void error(Exception e) {
		logger.error(e);
	}

	public void close(){
		interrupt();
		try{
			join();
		}
		catch (InterruptedException e) {
			logger.info("Command thread {} interrupted.", getName());
		}
		logger.info("Command thread {} closed.", getName());
	}
}


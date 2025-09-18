package org.tonylimps.liberreach.core.threads;

import com.alibaba.fastjson2.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ProfileManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.tonylimps.liberreach.core.enums.CommandType.HEARTBEAT;

public class CommandThread extends Thread{

	private final Logger logger = LogManager.getLogger(getClass());
	protected AtomicBoolean running;

	protected ProfileManager profileManager;
	protected Profile profile;
	protected ResourceBundle bundle;
	protected BufferedReader in;
	protected PrintWriter out;
	protected InetAddress address;
	protected ExceptionManager exceptionManager;
	protected Token token;
	protected UpdateThread updateThread;

	public CommandThread(){}

	@Override
	public void run() {
		while (running.get()) {
			try {
				String commandString = in.readLine();
				if(commandString == null){
					break;
				}
				if( !commandString.contains("\"type\":\"" + HEARTBEAT.getCode() +"\"") && !commandString.contains("\"answerType\":\"" + HEARTBEAT.getCode() +"\"") ){
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

	// 发送命令
	public void send(String command) {
		out.println(command);
		if(!command.contains("\"type\":\"" + HEARTBEAT.getCode() +"\"") && !command.contains("\"answerType\":\"" + HEARTBEAT.getCode() +"\"")){
			logger.info("Sent to {}:\n{}", address, command);
		}
	}

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


package com.tonylimps.filerelay.windows;

import com.tonylimps.filerelay.core.threads.ConnectThread;
import com.tonylimps.filerelay.core.threads.HeartBeatThread;
import com.tonylimps.filerelay.core.threads.TokenThread;
import com.tonylimps.filerelay.windows.managers.WindowManager;
import com.tonylimps.filerelay.windows.managers.WindowsExceptionManager;
import com.tonylimps.filerelay.windows.managers.WindowsProfileManager;
import com.tonylimps.filerelay.windows.managers.WindowsResourceBundleManager;
import com.tonylimps.filerelay.windows.threads.WindowsUpdateThread;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application{

	private static AtomicBoolean running;
	private static final Logger logger = LogManager.getLogger(Main.class);

	// managers
	private static WindowsProfileManager profileManager;
	private static WindowsExceptionManager exceptionManager;
	private static WindowsResourceBundleManager bundleManager;

	// threads
	private static TokenThread tokenThread;
	private static WindowsUpdateThread updateThread;
	private static ConnectThread connectThread;
	private static HeartBeatThread heartBeatThread;

	public static void main(String[] args) {
		logger.info("Program started.");
		running = new AtomicBoolean(true);
		exceptionManager = new WindowsExceptionManager();
		profileManager = new WindowsProfileManager(exceptionManager);
		bundleManager = new WindowsResourceBundleManager(exceptionManager, profileManager.getProfile());

		updateThread = new WindowsUpdateThread(exceptionManager, running);
		updateThread.start();
		logger.info("UI update thread started.");
		tokenThread = new TokenThread(exceptionManager, running, updateThread);
		tokenThread.start();
		logger.info("Token thread started.");
		connectThread = new ConnectThread(exceptionManager, running, profileManager.getProfile(), tokenThread.getToken());
		connectThread.start();
		logger.info("Connect thread started.");
		heartBeatThread = new HeartBeatThread(exceptionManager, running, profileManager.getProfile(), tokenThread.getToken(), connectThread);
		heartBeatThread.start();
		logger.info("Heartbeat thread started.");

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			ResourceBundle bundle = bundleManager.getBundle();
			WindowManager.initWindow("settings", "/fxmls/settings.fxml", bundle);
			WindowManager.initWindow("main", "/fxmls/main.fxml", bundle);
			WindowManager.initWindow("add", "/fxmls/add.fxml", bundle);
			WindowManager.show("main");
			logger.info("Application started.");
		}
		catch (Exception e) {
			logger.fatal("Load UI content failed.");
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	public static void exit(int code){
		running.set(false);
		tokenThread.close();
		connectThread.close();
		heartBeatThread.close();
		logger.info("Application exited with code "+code+".");
		System.exit(code);
	}

	public static WindowsProfileManager getProfileManager() {
		return profileManager;
	}

	public static WindowsExceptionManager getExceptionManager() {
		return exceptionManager;
	}

	public static WindowsResourceBundleManager getResourceBundleManager() {
		return bundleManager;
	}

	public static TokenThread getTokenThread() {
		return tokenThread;
	}

	public static ConnectThread getConnectThread() {
		return connectThread;
	}

}


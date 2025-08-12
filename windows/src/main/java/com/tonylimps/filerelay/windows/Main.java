package com.tonylimps.filerelay.windows;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.threads.ConnectThread;
import com.tonylimps.filerelay.core.threads.HeartBeatThread;
import com.tonylimps.filerelay.core.threads.TokenThread;
import com.tonylimps.filerelay.windows.threads.UpdateThread;
import com.tonylimps.filerelay.windows.managers.WindowManager;
import com.tonylimps.filerelay.windows.managers.WindowsExceptionManager;
import com.tonylimps.filerelay.windows.managers.WindowsProfileManager;
import com.tonylimps.filerelay.windows.managers.WindowsResourceBundleManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application{

	private static AtomicBoolean DEBUG;
	private static AtomicBoolean running;
	private static final Logger logger = LogManager.getLogger(Main.class);

	// managers
	private static WindowsProfileManager profileManager;
	private static WindowsExceptionManager exceptionManager;
	private static WindowsResourceBundleManager bundleManager;

	// threads
	private static TokenThread tokenThread;
	private static UpdateThread updateThread;
	private static ConnectThread connectThread;
	private static HeartBeatThread heartBeatThread;

	public static void main(String[] args) {
		running = new AtomicBoolean(true);
		DEBUG = new AtomicBoolean(Boolean.parseBoolean(Core.getConfig("debug")));
		if (args.length != 0) {
			if (args[0].equals("--debug")) {
				DEBUG = new AtomicBoolean(true);
			}
		}
		exceptionManager = new WindowsExceptionManager();
		profileManager = new WindowsProfileManager(exceptionManager);
		bundleManager = new WindowsResourceBundleManager(exceptionManager, profileManager.getProfile());

		tokenThread = new TokenThread(exceptionManager, running);
		tokenThread.start();
		updateThread = new UpdateThread(exceptionManager, running);
		updateThread.start();
		connectThread = new ConnectThread(exceptionManager, running, profileManager.getProfile(), tokenThread.getToken(), DEBUG);
		connectThread.start();
		heartBeatThread = new HeartBeatThread(exceptionManager, running, profileManager.getProfile(), tokenThread.getToken(), connectThread, DEBUG);
		heartBeatThread.start();

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			ResourceBundle bundle = bundleManager.getBundle();
			WindowManager.initWindow("settings", "/com/tonylimps/filerelay/windows/fxmls/settings.fxml", bundle);
			WindowManager.initWindow("main", "/com/tonylimps/filerelay/windows/fxmls/main.fxml", bundle);
			WindowManager.initWindow("add", "/com/tonylimps/filerelay/windows/fxmls/add.fxml", bundle);
			WindowManager.show("main");
			WindowManager.getStage("main").setTitle(bundle.getString("main.title"));
			WindowManager.getStage("main").setOnCloseRequest( event -> exit(0));
		}
		catch (Exception e) {
			exceptionManager.throwException(e);
		}
	}

	private static void exit(int code){
		running.set(false);
		tokenThread.interrupt();
		connectThread.interrupt();
		heartBeatThread.interrupt();
		try {
			tokenThread.join();
			connectThread.join();
			heartBeatThread.join();
		}
		catch (InterruptedException e) {
			exceptionManager.throwException(e);
		}
		System.exit(code);
	}



	public static boolean isRunning() {
		return running.get();
	}

	public static boolean isDebug() {
		return DEBUG.get();
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

package com.tonylimps.filerelay.windows;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.threads.ConnectThread;
import com.tonylimps.filerelay.core.threads.HeartBeatThread;
import com.tonylimps.filerelay.core.threads.TokenThread;
import com.tonylimps.filerelay.windows.managers.WindowManager;
import com.tonylimps.filerelay.windows.managers.WindowsExceptionManager;
import com.tonylimps.filerelay.windows.managers.WindowsProfileManager;
import com.tonylimps.filerelay.windows.managers.WindowsResourceBundleManager;
import com.tonylimps.filerelay.windows.threads.UpdateThread;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application{

	private static AtomicBoolean running;

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
		Core.getLogger().info("Program started.");
		running = new AtomicBoolean(true);
		exceptionManager = new WindowsExceptionManager();
		profileManager = new WindowsProfileManager(exceptionManager);
		bundleManager = new WindowsResourceBundleManager(exceptionManager, profileManager.getProfile());

		tokenThread = new TokenThread(exceptionManager, running);
		tokenThread.start();
		Core.getLogger().info("Token thread started.");
		updateThread = new UpdateThread(exceptionManager, running);
		updateThread.start();
		Core.getLogger().info("UI update thread started.");
		connectThread = new ConnectThread(exceptionManager, running, profileManager.getProfile(), tokenThread.getToken());
		connectThread.start();
		Core.getLogger().info("Connect thread started.");
		heartBeatThread = new HeartBeatThread(exceptionManager, running, profileManager.getProfile(), tokenThread.getToken(), connectThread);
		heartBeatThread.start();
		Core.getLogger().info("Heartbeat thread started.");

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			ResourceBundle bundle = bundleManager.getBundle();
			WindowManager.initWindow("settings", "/com/tonylimps/filerelay/windows/fxmls/settings.fxml", bundle);
			WindowManager.initWindow("main", "/com/tonylimps/filerelay/windows/fxmls/main.fxml", bundle);
			WindowManager.initWindow("add", "/com/tonylimps/filerelay/windows/fxmls/add.fxml", bundle);
			WindowManager.getStage("main").setTitle(bundle.getString("main.title"));
			WindowManager.getStage("main").setOnCloseRequest( event -> exit(0));
			WindowManager.show("main");
			Core.getLogger().info("Application started.");
		}
		catch (Exception e) {
			Core.getLogger().fatal("Load UI content failed.");
			exceptionManager.throwException(e);
		}
	}

	private static void exit(int code){
		running.set(false);
		tokenThread.close();
		connectThread.close();
		heartBeatThread.close();
		Core.getLogger().info("Application exited with code "+code+".");
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
